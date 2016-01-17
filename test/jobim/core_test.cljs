(ns jobim.core-test ^:figwheel-always
  (:require [jobim.core :as jobim :refer-macros [clojure-code]]
            [cljs.test :refer-macros [deftest is testing run-tests]]
						[jobim.figwheel.helper]))

;; Note: I haven't figured out how to correctly use fixtures in cljs tests
;; If you'd like to ping me and show me a good working example, I'd be very
;; thankful!

(def slides
  {:title (jobim/->Title "Test" "Mock")
   :text (jobim/->Text "hello world")
   :picture (jobim/->Picture "img.png")
   :captioned (jobim/->CaptionedPic "img.png" "this is caption")
   :clojure-code (jobim/->ClojureCode '((+ 1 2) (+ 3 4)) {} 40)
   :custom-slide (jobim/->CustomSlide [:div])
   :code (jobim/->Code "js" "function(){ return 1; }")})

(def show
  [(:title slides)
   (:picture slides)])

(deftest stds-test
  (testing "std-next"
    (is (= (jobim/std-next {} {:page 19}) {:page 20})))
  (testing "std-prev"
    (is (= (jobim/std-prev {} {:page 19}) {:page 18}))))

(deftest center-test
  (is (= (jobim/center 80 [:div])
         [:div {:style {:width "80%"
                        :margin-left "auto"
                        :margin-right "auto"}}
          [:div]])))

(deftest slide-test
  (testing "render-slide"
    (testing "Title"
      (is (= (jobim/render-slide (:title slides))
             [:div
              {:style jobim/title-style}
              (jobim/center 80 [:h1 {:style jobim/h1-style} "Test"])
              (jobim/center 66.6 [:h2 {:style jobim/h2-style} "Mock"])])))
    (testing "Text"
      (is (= (jobim/render-slide (:text slides))
             [:div {:style jobim/title-style}
              (jobim/center 80 [:h1 {:style jobim/h1-style} "hello world"])])))
    (testing "Picture"
      (is (= (jobim/render-slide (:picture slides))
             [:div
              {:style (merge jobim/flexbox {:height "50%" :width "50%"})}
              [:img {:src "img.png" :style jobim/pic-style}]])))
    (testing "CaptionedPic"
      (is (= (jobim/render-slide (:captioned slides))
             [:div {:style (merge jobim/flexbox {:flex-direction "column"})}
              (jobim/render-slide (:picture slides))
              (jobim/center 80
                            [:div {:style
                                   {:padding-top "50px" :text-align "center"}}
                             "this is caption"])])))
    (testing "ClojureCode"
      (is (= (first (jobim/render-slide (:clojure-code slides))) :div))
      (is (= (second (jobim/render-slide (:clojure-code slides)))
             {:style {:text-align "left"}}))
      (is (= (count (nth (jobim/render-slide (:clojure-code slides)) 2)) 2)))
    (testing "CustomSlide"
      (is (= (jobim/render-slide (:custom-slide slides)) [:div])))
    (testing "Code"
      (is (= (first (jobim/render-slide (:code slides))) :pre))
      (is (= (-> (jobim/render-slide (:code slides)) second first) :code))
      (is (= (-> (jobim/render-slide (:code slides)) second second :class) "js"))))
  (testing "next-slide"
    (testing "Title"
      (is (= (jobim/next-slide (:title slides) {:page 1}) {:page 2})))
    (testing "Picture"
      (is (= (jobim/next-slide (:picture slides) {:page 1}) {:page 2})))
    (testing "CaptionedPic"
      (is (= (jobim/next-slide (:captioned slides) {:page 1}) {:page 2})))
    (testing "ClojureCode"
      (is (= (jobim/next-slide (:clojure-code slides) {:page 1}) {:page 2})))
    (testing "Text"
      (is (= (jobim/next-slide (:text slides) {:page 1}) {:page 2})))
    (testing "CustomSlide"
      (is (= (jobim/next-slide (:custom-slide slides) {:page 1}) {:page 2})))
    (testing "Code"
      (is (= (jobim/next-slide (:code slides) {:page 1}) {:page 2}))))
  (testing "prev-slide"
    (testing "Title"
      (is (= (jobim/prev-slide (:title slides) {:page 1}) {:page 0})))
    (testing "Picture"
      (is (= (jobim/prev-slide (:picture slides) {:page 1}) {:page 0})))
    (testing "CaptionedPic"
      (is (= (jobim/prev-slide (:captioned slides) {:page 1}) {:page 0})))
    (testing "ClojureCode"
      (is (= (jobim/prev-slide (:captioned slides) {:page 1}) {:page 0})))
    (testing "Text"
      (is (= (jobim/prev-slide (:text slides) {:page 1}) {:page 0})))
    (testing "CustomSlide"
      (is (= (jobim/prev-slide (:custom-slide slides) {:page 1}) {:page 0})))
    (testing "Code"
      (is (= (jobim/prev-slide (:code slides) {:page 1}) {:page 0})))))

(deftest curr-slide
  (is (= (jobim/curr-slide [:a :b :c] {:page 0}) :a))
  (is (= (jobim/curr-slide [:a :b :c] {:page 1}) :b))
  (is (= (jobim/curr-slide [:a :b :c] {:page 2}) :c)))

(deftest guard
  (testing "decrement"
    (is (= (jobim/guard {:page 10} [:a :b :c]) {:page 2}))
    (is (= (jobim/guard {:page 3} [:a :b :c]) {:page 2}))
    (is (= (jobim/guard {:page 3 :ect 10} [:a :b :c]) {:page 2 :ect 10}))
    (is (= (jobim/guard {:page 19} [:a :b :c :d :e :f]) {:page 5})))
  (testing "increment"
    (is (= (jobim/guard {:page -1} [1]) {:page 0}))
    (is (= (jobim/guard {:page -100} []) {:page 0}))
    (is (= (jobim/guard {:page -12} [2 2 3]) {:page 0}))
    (is (= (jobim/guard {:page -14 :n -10} [:a :c :e :d]) {:page 0 :n -10})))
  (testing "no change"
    (is (= (jobim/guard {:page 3 :etc 1} [:a :b :c :f]) {:page 3 :etc 1}))
    (is (= (jobim/guard {:page 1 :l 2} [:a :b :c :d :e]) {:page 1 :l 2}))
    (is (= (jobim/guard {:page 0} [:a :b :c]) {:page 0}))
    (is (= (jobim/guard {:page 4} [:a :b :c :f :e]) {:page 4}))))

(deftest render-show-test
  (testing "Paginaton"
    (is (= (jobim/render-show show {:page 0} jobim/default-style)
           [:div {:style jobim/outer-style}
            [:div
             {:style jobim/default-style}
             (jobim/render-slide (:title slides))]]))
    (is (= (jobim/render-show show {:page 1} jobim/default-style)
           [:div {:style jobim/outer-style}
            [:div
             {:style jobim/default-style}
             (jobim/render-slide (:picture slides))]]))))

(deftest indent-test
  (is (= (jobim/indent 0 "1 + 2") "1 + 2"))
  (is (= (jobim/indent 1 "1 + 2") "  1 + 2"))
  (is (= (jobim/indent 2 "1 + 2") "    1 + 2")))

(deftest indent*-test
  (is (= ((jobim/indent* 0) "1 + 2") "1 + 2"))
  (is (= ((jobim/indent* 1) "1 + 2") "  1 + 2"))
  (is (= ((jobim/indent* 2) "1 + 2") "    1 + 2"))
  (is (= ((jobim/indent* -1) ["1 + 2"]) ["1 + 2"]))
  (is (= ((jobim/indent* 0) ["1 + 2"]) ["  1 + 2"]))
  (is (= ((jobim/indent* 1) ["1 + 2"]) ["    1 + 2"]))
  (is (= ((jobim/indent* -1) ["add():" ["1 + 2"]])  ["add():" ["  1 + 2"]]))
  (is (= ((jobim/indent* 0)  ["add():" ["1 + 2"]])  ["  add():" ["    1 + 2"]]))
  (is (= ((jobim/indent* 1)  ["add():" ["1 + 2"]])  ["    add():" ["      1 + 2"]])))

(deftest nl
  (is (= (jobim/nl "") "\n"))
  (is (= (jobim/nl "1 + 2") "1 + 2\n")))

(deftest nl*
  (is (= (jobim/nl* ["1 + 2" "3 + 4"]) ["1 + 2\n" "3 + 4"]))
  (is (= (jobim/nl* ["3 + 4"]) ["3 + 4"])))

(def js-code
  (jobim/code "js"
    "function e(a, b){"
    ["var c = a + 1;"
     "var d = b + 2;"
     "return c * d;"]
    "};"
    "e(10, 20);"))

(def js-code-expect
  (jobim/->Code "js"
    (str "function e(a, b){\n"
         "  var c = a + 1;\n"
         "  var d = b + 2;\n"
         "  return c * d;\n"
         "};\n"
         "e(10, 20);")))

(def py-code
  (jobim/code "py"
    "def e(a, b):"
    ["c = a + 1"
     "d = b + 2"
     "return c * d"]
    "e(10, 20)"))

(def py-code-expect
  (jobim/->Code "py"
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
