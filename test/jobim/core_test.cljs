(ns jobim.core-test ^:figwheel-always
  (:require [jobim.core :as jobim :refer-macros [clojure-code]]
            [cljs.test :refer-macros [deftest is testing run-tests]]))

(def a 1)
(def b 2)

(def slides
  {:title (jobim/->Title "Test" "Mock")
   :picture (jobim/->Picture "img.png")
   :captioned (jobim/->CaptionedPic "img.png" "this is caption")
   :clojure-code (jobim/clojure-code (+ a 1) (+ b 2)) })

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
              [:h1 {:style jobim/h1-style} "Test"]
              (jobim/center 66.6 [:h2 {:style jobim/h2-style} "Mock"])])))
    (testing "Picture"
      (is (= (jobim/render-slide (:picture slides))
             [:div
              {:style (merge jobim/flexbox {:height "50%" :width "50%"})}
              [:img {:src "img.png" :style jobim/pic-style}]])))
    (testing "CaptionedPic"
      (is (= (jobim/render-slide (:captioned slides))
             [:div {:style (merge jobim/flexbox {:flex-direction "column"})}
              (jobim/render-slide (:picture slides))
              [:div {:style {:padding-top "50px"}} "this is caption"]])))
    (testing "ClojureCode"
      (is (= (jobim/render-slide (:clojure-code slides))
             [:div
              {:style (merge jobim/flexbox {:flex-direction "column"})}
              (seq
               [[:div {:key 0}
                 [:pre
                  [:code "(+ a 1)"]]]
                [:div {:key 1}
                 [:pre
                  [:code "(+ b 2)"]]]])]))))
  (testing "next-slide"
    (testing "Title"
      (is (= (jobim/next-slide (:title slides) {:page 1}) {:page 2})))
    (testing "Picture"
      (is (= (jobim/next-slide (:picture slides) {:page 1}) {:page 2})))
    (testing "CaptionedPic"
      (is (= (jobim/next-slide (:captioned slides) {:page 1}) {:page 2})))
    (testing "ClojureCode"
      (is (= (jobim/next-slide (:clojure-code slides) {:page 1}) {:page 2}))))
  (testing "prev-slide"
    (testing "Title"
      (is (= (jobim/prev-slide (:title slides) {:page 1}) {:page 0})))
    (testing "Picture"
      (is (= (jobim/prev-slide (:picture slides) {:page 1}) {:page 0})))
    (testing "CaptionedPic"
      (is (= (jobim/prev-slide (:captioned slides) {:page 1}) {:page 0})))
    (testing "ClojureCode"
      (is (= (jobim/prev-slide (:captioned slides) {:page 1}) {:page 0})))))

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

#_(deftest indent-clj-test
  (is (= (jobim/indent-clj '(+ 1 2) "(+ 1 2)"))))
