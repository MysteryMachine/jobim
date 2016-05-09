(ns jobim.core-test ^:figwheel-always
  (:require [jobim.core :as jobim]
            [jobim.core.impl :as impl]
            [jobim.protocols :as protocols]
            [cljs.test :refer-macros [deftest is testing run-tests]]
						[jobim.figwheel.helper])
  (:require-macros [jobim.core :as jobim]
                   [jobim.core.impl :as impl]))

;; Fixtures

(def slides
  {:title (jobim/title "Test" "Mock")
   :text (jobim/text "hello world")
   :picture (jobim/img "img.png")
   :captioned (jobim/captioned-img "img.png" "this is caption")
   :clojure-code (jobim/clojure-code 80 (+ 1 2) (+ 3 4) {} 40)
   :custom-slide (jobim/custom-slide (fn [state] [:div (:text state)]))
   :code (jobim/code* "js" ["function plus(a,b){"
                            ["return a + b;"]
                            "}"])})

(def show
  [(:title slides)
   (:picture slides)])

;; Protocols

(deftest stds-test
  (testing "std-next"
    (is (= (protocols/std-next {} {:page 19 :index 1}) {:page 20 :index 0})))
  (testing "std-prev"
    (is (= (protocols/std-prev {} {:page 19 :index 10}) {:page 18 :index 0})))
  (testing "std-up"
    (is (= (protocols/std-up {} {:page 19 :index 0}) {:page 19 :index 1})))
  (testing "std-do"
    (is (= (protocols/std-down {} {:page 19 :index 1}) {:page 19 :index 0}))))

(deftest center-test
  (is (= (impl/center 80 [:div])
         [:div {:style {:width "80%"
                        :margin-left "auto"
                        :margin-right "auto"}}
          [:div]])))

(deftest slide-test
  (testing "render-slide"
    (testing "Title"
      (is (= (protocols/render-slide (:title slides) {})
             [:div
              {:style impl/title-style}
              (impl/center 80 [:h1 {:style impl/h1-style :class "jobim-title"} "Test"])
              (impl/center 66.6 [:h2 {:style impl/h2-style :class "jobim-subtitle"} "Mock"])])))
    (testing "Text"
      (is (= (protocols/render-slide (:text slides) {})
             [:div {:style impl/title-style}
              (impl/center 80 [:h1 {:style impl/h1-style :class "jobim-text"} "hello world"])])))
    (testing "Picture"
      (is (= (protocols/render-slide (:picture slides) {})
             [:div
              {:style (merge impl/flexbox {:height "50%" :width "50%"})}
              [:img {:src "img.png" :style impl/pic-style}]])))
    (testing "CaptionedPic"
      (is (= (protocols/render-slide (:captioned slides) {})
             [:div {:style (merge impl/flexbox {:flex-direction "column"})}
              (protocols/render-slide (:picture slides) {})
              (impl/center 80
                           [:div {:class "jobim-caption"
                                  :style
                                   {:padding-top "50px" :text-align "center"}}
                             "this is caption"])])))
    (testing "ClojureCode"
      (is (= (first (protocols/render-slide (:clojure-code slides) {})) :div))
      (is (= (second (protocols/render-slide (:clojure-code slides) {}))
             {:style {:text-align "left"}}))
      (is (= (count (nth (protocols/render-slide (:clojure-code slides) {}) 2)) 4)))
    (testing "CustomSlide"
      (is (= (protocols/render-slide (:custom-slide slides) {:text "pass"}) [:div "pass"]))) 
    (testing "Code"
      (is (= (first (protocols/render-slide (:code slides) {})) :pre))
      (is (= (-> (protocols/render-slide (:code slides) {}) second first) :code))
      (is (= (-> (protocols/render-slide (:code slides) {}) second second :class) "js"))))
  (testing "next-slide" 
    (testing "Title"
      (is (= (protocols/next-slide (:title slides) {:page 1 :index 1}) {:page 2 :index 0})))
    (testing "Picture"
      (is (= (protocols/next-slide (:picture slides) {:page 1 :index 1}) {:page 2 :index 0})))
    (testing "CaptionedPic"
      (is (= (protocols/next-slide (:captioned slides) {:page 1 :index 1}) {:page 2 :index 0})))
    (testing "ClojureCode"
      (is (= (protocols/next-slide (:clojure-code slides) {:page 1 :index 1}) {:page 2 :index 0})))
    (testing "Text"
      (is (= (protocols/next-slide (:text slides) {:page 1 :index 1}) {:page 2 :index 0})))
    (testing "CustomSlide"
      (is (= (protocols/next-slide (:custom-slide slides) {:page 1 :index 1}) {:page 2 :index 0})))
    (testing "Code"
      (is (= (protocols/next-slide (:code slides) {:page 1 :index 1}) {:page 2 :index 0}))))
  (testing "prev-slide"
    (testing "Title"
      (is (= (protocols/prev-slide (:title slides) {:page 1 :index 1}) {:page 0 :index 0})))
    (testing "Picture"
      (is (= (protocols/prev-slide (:picture slides) {:page 1 :index 1}) {:page 0 :index 0})))
    (testing "CaptionedPic"
      (is (= (protocols/prev-slide (:captioned slides) {:page 1 :index 1}) {:page 0 :index 0})))
    (testing "ClojureCode"
      (is (= (protocols/prev-slide (:captioned slides) {:page 1 :index 1}) {:page 0 :index 0})))
    (testing "Text"
      (is (= (protocols/prev-slide (:text slides) {:page 1 :index 1}) {:page 0 :index 0})))
    (testing "CustomSlide"
      (is (= (protocols/prev-slide (:custom-slide slides) {:page 1 :index 1}) {:page 0 :index 0})))
    (testing "Code"
      (is (= (protocols/prev-slide (:code slides) {:page 1 :index 1}) {:page 0 :index 0})))))

(deftest curr-slide
  (is (= (jobim/curr-slide [:a :b :c] {:page 0}) :a))
  (is (= (jobim/curr-slide [:a :b :c] {:page 1}) :b))
  (is (= (jobim/curr-slide [:a :b :c] {:page 2}) :c)))

(deftest guard
  (testing "decrement"
    (is (= (impl/guard {:page 10} [:a :b :c]) {:page 2}))
    (is (= (impl/guard {:page 3} [:a :b :c]) {:page 2}))
    (is (= (impl/guard {:page 3 :ect 10} [:a :b :c]) {:page 2 :ect 10}))
    (is (= (impl/guard {:page 19} [:a :b :c :d :e :f]) {:page 5})))
  (testing "increment"
    (is (= (impl/guard {:page -1} [1]) {:page 0}))
    (is (= (impl/guard {:page -100} [:a]) {:page 0}))
    (is (= (impl/guard {:page -12} [2 2 3]) {:page 0}))
    (is (= (impl/guard {:page -14 :n -10} [:a :c :e :d]) {:page 0 :n -10})))
  (testing "no change"
    (is (= (impl/guard {:page 3 :etc 1} [:a :b :c :f]) {:page 3 :etc 1}))
    (is (= (impl/guard {:page 1 :l 2} [:a :b :c :d :e]) {:page 1 :l 2}))
    (is (= (impl/guard {:page 0} [:a :b :c]) {:page 0}))
    (is (= (impl/guard {:page 4} [:a :b :c :f :e]) {:page 4}))))

(deftest render-show-test
  (testing "Paginaton"
    (is (= (impl/render-show show {:page 0} jobim/default-style)
           [:div {:style impl/outer-style}
            [:div
             {:style jobim/default-style}
             (protocols/render-slide (:title slides) {})]]))
    (is (= (impl/render-show show {:page 1} jobim/default-style)
           [:div {:style impl/outer-style}
            [:div
             {:style jobim/default-style}
             (protocols/render-slide (:picture slides) {})]]))))

(deftest indent-test
  (is (= (impl/indent 0 "1 + 2") "1 + 2"))
  (is (= (impl/indent 1 "1 + 2") "  1 + 2"))
  (is (= (impl/indent 2 "1 + 2") "    1 + 2")))

(deftest indent*-test
  (is (= ((impl/indent* 0) "1 + 2") "1 + 2"))
  (is (= ((impl/indent* 1) "1 + 2") "  1 + 2"))
  (is (= ((impl/indent* 2) "1 + 2") "    1 + 2"))
  (is (= ((impl/indent* -1) ["1 + 2"]) ["1 + 2"]))
  (is (= ((impl/indent* 0) ["1 + 2"]) ["  1 + 2"]))
  (is (= ((impl/indent* 1) ["1 + 2"]) ["    1 + 2"]))
  (is (= ((impl/indent* -1) ["add():" ["1 + 2"]])  ["add():" ["  1 + 2"]]))
  (is (= ((impl/indent* 0)  ["add():" ["1 + 2"]])  ["  add():" ["    1 + 2"]]))
  (is (= ((impl/indent* 1)  ["add():" ["1 + 2"]])  ["    add():" ["      1 + 2"]])))

(deftest nl
  (is (= (impl/nl "") "\n"))
  (is (= (impl/nl "1 + 2") "1 + 2\n")))

(deftest nl*
  (is (= (impl/nl* ["1 + 2" "3 + 4"]) ["1 + 2\n" "3 + 4"]))
  (is (= (impl/nl* ["3 + 4"]) ["3 + 4"])))

(def js-code
  (jobim/code* "js"
    "function e(a, b){"
    ["var c = a + 1;"
     "var d = b + 2;"
     "return c * d;"]
    "};"
    "e(10, 20);"))

(def js-code-expect
  (impl/->Code "js"
    (str "function e(a, b){\n"
         "  var c = a + 1;\n"
         "  var d = b + 2;\n"
         "  return c * d;\n"
         "};\n"
         "e(10, 20);")))

(def py-code
  (jobim/code* "py"
    "def e(a, b):"
    ["c = a + 1"
     "d = b + 2"
     "return c * d"]
    "e(10, 20)"))

(def py-code-expect
  (impl/->Code "py"
               (str "def e(a, b):\n"
                    "  c = a + 1\n"
                    "  d = b + 2\n"
                    "  return c * d\n"
                    "e(10, 20)")))

(deftest code-test
  (testing "js-code"
    (is (= js-code js-code-expect)))
  (testing "py-code"
    (is (= py-code py-code-expect))))

(def clj
  (jobim/clojure-code 40
   (+ 1 2)
   (def a (+ 1 2 3))
   (defn b [c d] (+ a c d))
   (b a a)
   (defn d [e f] (b a (- e f)))
   (d 10 5)
   (defn n [b d] (+ b d))
   (n 10 20)
   1
   :a
   "b"
   (def atm (atom 0))
   @atm
   (swap! atm inc)
   @atm))

(def clj-env (:env clj))

(def simple-clj
  (jobim/clojure-code 40
   (+ 1 2)
   (def a (+ 1 2 3))
   (defn b [c d] (+ a c d))))

(deftest clj-test
  (testing "env"
    (is (= (get clj-env :%0) 3))
    (is (= (get clj-env :%3) 18))
    (is (= (get clj-env :%5) 17))
    (is (= (get clj-env :%7) 30))
    (is (= (get clj-env :%8) 1))
    (is (= (get clj-env :%9) :a))
    (is (= (get clj-env :%10) "b"))
    (is (= (get clj-env :%12) 0))
    (is (= (get clj-env :%14) 1))
    (is (= @(get clj-env :atm) 1))
    (is (not (nil? (get clj-env :a))))
    (is (not (nil? (get clj-env :b))))
    (is (not (nil? (get clj-env :d))))
    (is (not (nil? (get clj-env :n)))))
  (testing "code"
    (is (= (:code simple-clj)
           '((+ 1 2)
             (def a (+ 1 2 3))
             (defn b [c d] (+ a c d))))))
  (testing "pprint-width"
    (is (= (:pprint-width clj 40)))))

(run-tests)
