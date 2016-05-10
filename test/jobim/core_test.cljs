(ns jobim.core-test ^:figwheel-always
  (:require [jobim.core :as jobim]
            [jobim.core.impl :as impl]
            [jobim.protocols :as protocols]
            [cljs.test :refer-macros [deftest is testing run-tests]]
						[jobim.figwheel.helper])
  (:require-macros [jobim.core :as jobim]
                   [jobim.core.impl :as impl]))

(def slides
  {:title (jobim/title "Test" "Mock")
   :text (jobim/text "hello world")
   :picture (jobim/img "img.png")
   :captioned (jobim/captioned-img "img.png" "this is caption")
   :bullets (jobim/bullets "title" "a" "b" "c")
   :clojure-code (jobim/clojure-code 80 (+ 1 2) (+ 3 4) {} 40)
   :custom-slide (jobim/custom-slide (fn [state] [:div (:text state)]))
   :code (jobim/code* "js" ["function plus(a,b){"
                            ["return a + b;"]
                            "}"])})

(def show
  [(:title slides)
   (:picture slides)])

(jobim/defclj my-code 80 (+ 1 2) (+ 3 4) {} 40)

(deftest defclj-test
  (is (= my-code (:clojure-code slides))))

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
    (testing "BulletedList"
      (is (= (protocols/render-slide (:picture slides) {})
             [:div
              {:style (merge impl/flexbox
                             {:height "50%"
                              :width "50%"})}
              [:img {:src "img.png"
                     :style {:height "100%"
                             :width "auto"
                             :outline "10px #0E0E0E solid"}}]] )))
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
    (testing "BulletedList"
      (is (= (protocols/next-slide (:bullets slides) {:page 1 :index 1}) {:page 2 :index 0})))
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
    (testing "BulletedList"
      (is (= (protocols/prev-slide (:bullets slides) {:page 1 :index 1}) {:page 0 :index 0})))
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

(deftest guard-page-test
  (testing "decrement"
    (is (= (impl/guard-page {:page 10} [:a :b :c]) {:page 2}))
    (is (= (impl/guard-page {:page 3} [:a :b :c]) {:page 2}))
    (is (= (impl/guard-page {:page 3 :ect 10} [:a :b :c]) {:page 2 :ect 10}))
    (is (= (impl/guard-page {:page 19} [:a :b :c :d :e :f]) {:page 5})))
  (testing "increment"
    (is (= (impl/guard-page {:page -1} [1]) {:page 0}))
    (is (= (impl/guard-page {:page -100} [:a]) {:page 0}))
    (is (= (impl/guard-page {:page -12} [2 2 3]) {:page 0}))
    (is (= (impl/guard-page {:page -14 :n -10} [:a :c :e :d]) {:page 0 :n -10})))
  (testing "no change"
    (is (= (impl/guard-page {:page 3 :etc 1} [:a :b :c :f]) {:page 3 :etc 1}))
    (is (= (impl/guard-page {:page 1 :l 2} [:a :b :c :d :e]) {:page 1 :l 2}))
    (is (= (impl/guard-page {:page 0} [:a :b :c]) {:page 0}))
    (is (= (impl/guard-page {:page 4} [:a :b :c :f :e]) {:page 4}))))

(defrecord T [i]
  protocols/Indexable
  (up-slide [this _] this)
  (down-slide [this _] this)
  (max-index [this] i))

(deftest guard-index-test
  (testing "decrement"
    (is (= (impl/guard-index {:index 10 :page 0}  [(->T 3) :b :c])
           {:page 0 :index 3}))
    (is (= (impl/guard-index {:index 3 :page 1} [:a (->T 3) :c])
           {:page 1 :index 3}))
    (is (= (impl/guard-index {:index 3 :ect 10 :page 1} [:a (->T 2) :c])
           {:page 1 :ect 10 :index 2}))
    (is (= (impl/guard-index {:index 19 :page 5} [:a :b :c :d :e (->T 18)])
           {:page 5 :index 18})))
  (testing "increment"
    (is (= (impl/guard-index {:index -1 :page 1} [1 (->T 1)])
           {:page 1 :index 0}))
    (is (= (impl/guard-index {:index -100 :page 0} [(->T 11) :a])
           {:page 0 :index 0}))
    (is (= (impl/guard-index {:index -12 :page 4} [2 2 3 1 (->T 10)])
           {:page 4 :index 0}))
    (is (= (impl/guard-index {:index -14 :n -10 :page 2} [:a :c (->T 11) :e :d])
           {:page 2 :n -10 :index 0}))) 
  (testing "no change"
    (is (= (impl/guard-index {:index 3 :etc 1 :page 0} [{} :b :c :f])
           {:page 0 :etc 1 :index 3}))
    (is (= (impl/guard-index {:index 1 :l 2 :page 1} [:a (->T 10) :b :c :d :e])
           {:page 1 :l 2 :index 1}))
    (is (= (impl/guard-index {:index 0 :page 2} [:a :b (->T 2) :c])
           {:page 2 :index 0}))
    (is (= (impl/guard-index {:index 9 :page 3} [:a :b :c (->T 9) :f :e])
           {:page 3 :index 9}))))

(def test-slides (vec (repeat 2 (->T 2))))

(deftest guard-test
  (= {:page 0 :index 2} (impl/guard {:page -1 :index 3} test-slides))
  (= {:page 2 :index 0} (impl/guard {:page 3 :index -1} test-slides))
  (= {:page 1 :index 0} (impl/guard {:page 1 :index -1} test-slides))
  (= {:page 0 :index 1} (impl/guard {:page -1 :index 1} test-slides))
  (= {:page 1 :index 2} (impl/guard {:page 1 :index 3} test-slides))
  (= {:page 2 :index 1} (impl/guard {:page 3 :index 1} test-slides))
  (= {:page 0 :index 0} (impl/guard {:page -1 :index -1} test-slides))
  (= {:page 2 :index 2} (impl/guard {:page 3 :index 3} test-slides))
  (= {:page 1 :index 1} (impl/guard {:page 1 :index 1} test-slides)))

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

(def simple-clj
  (jobim/clojure-code 40
   (+ 1 2)
   (def a (+ 1 2 3))
   (defn b [c d] (+ a c d))))

(deftest clj-test
  (let [{:keys [%0 %3 %5 %7 %8 %9
                %10 %12 %14 atm
                a b d n]} (jobim/env clj)]
   (testing "env"
     (is (= %0 3))
     (is (= %3 18))
     (is (= %5 17))
     (is (= %7 30))
     (is (= %8 1))
     (is (= %9 :a))
     (is (= %10 "b"))
     (is (= %12 0))
     (is (= %14 1))
     (is (= @atm 1))
     (is (not (nil? a)))
     (is (not (nil? b)))
     (is (not (nil? d)))
     (is (not (nil? n)))))
  (testing "code"
    (is (= (:code simple-clj)
           '((+ 1 2)
             (def a (+ 1 2 3))
             (defn b [c d] (+ a c d))))))
  (testing "pprint-width"
    (is (= (:pprint-width clj 40)))))

(def bullet-data {:class "jobim-li"})
(def bullets-data {:class "jobim-ul"})
(def bullets*-data {:class "jobim-ul"
                    :style {:font-size "0.8em"}})

(deftest render-bullet-test
  (is (= [:li bullet-data "ayy"] (impl/render-bullet "ayy")))
  (is (= [:ul bullets*-data
          [:li bullet-data "a"]
          [:li bullet-data "b"]
          [:ul bullets*-data
           [:li bullet-data "c"]]]
         (impl/render-bullet ["a" "b" ["c"]]))))

(deftest render-bullets-test
  (is (= [:ul bullets-data
          [:li bullet-data "ayy"]]
         (impl/render-bullets ["ayy"])))
  (is (= [:ul bullets-data
          [:li bullet-data "a"]
          [:li bullet-data "b"]
          [:ul bullets*-data
           [:li bullet-data "c"]]]
         (impl/render-bullets ["a" "b" ["c"]]))))

(run-tests)
