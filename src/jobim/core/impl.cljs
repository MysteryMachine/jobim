(ns jobim.core.impl
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [put! chan >! <!]]
            [fipp.clojure :refer [pprint]]
            [clojure.string :as string]
            [jobim.protocols :as protocols])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def flexbox
  {:display "flex"
   :align-items "center"
   :justify-content "center"
   :height "100%"
   :width "100%"})

(def title-style
  {:text-align "center" :width "100%"})
(def h1-style
  {:font-weight "100"
   :font-size "1.2em"
   :padding-bottom "1em"})
(def h2-style
  {:font-weight "100"
   :font-size "1em"})
(defn center [pct elem]
  [:div {:style {:width (str pct "%")
                 :margin-left "auto"
                 :margin-right "auto"}}
   elem])

(defrecord Title [title subtitle]
  protocols/Slide
  (render-slide [this state]
    (if-not (:css state)
     [:div
      {:style title-style
       :class "jobim-title-style"}
       (center 80
        [:h1 {:style h1-style :class "jobim-title"} title])
       (center 66.6
        [:h2 {:style h2-style :class "jobim-subtitle"} subtitle])]
     [:div.jobim-title-style
      [:div.jobim-center-80
       [:h1.jobim-title title]]
      [:div.jobim-center-66
       [:h2.jobim-subtitle subtitle]]]))
  (next-slide [this state] (protocols/std-next this state))
  (prev-slide [this state] (protocols/std-prev this state)))

(defrecord Text [text]
  protocols/Slide
  (render-slide [this state]
    [:div {:style (if-not (:css state) title-style {})}
     (if-not (:css state)
      (center 80
       [:div {:style h1-style :class "jobim-text"} text])
      [:div.job-center-80
       [:div.jobim-text text]])])
  (next-slide [this state] (protocols/std-next this state))
  (prev-slide [this state] (protocols/std-prev this state)))

(def pic-style
  {:height "100%"
   :width "auto"
   :outline "10px #0E0E0E solid"})

(defrecord Picture [src]
  protocols/Slide
  (render-slide [this state]
    [:div.jobim-flexbox.pic
     {:style (if (:css state) {}
               (merge flexbox {:height "50%" :width "50%"}))}
     [:img.jobim-pic-style
      {:src src :style (if (:css state) {} pic-style)}]])
  (next-slide [this state] (protocols/std-next this state))
  (prev-slide [this state] (protocols/std-prev this state)))

(defrecord CaptionedPic [url caption]
  protocols/Slide
  (render-slide [this state]
    [:div.jobim-flexbox.captioned
     {:style (if (:css state) {}
              (merge flexbox {:flex-direction "column"}))}
     (protocols/render-slide (->Picture url) state)
     (if-not (:css state)
      (center 80
       [:div
        {:style {:padding-top "50px" :text-align "center"}
         :class "jobim-caption"}
        caption])
      [:div.jobim-center-80
       [:style.jobim-caption caption]])])
  (next-slide [this state] (protocols/std-next this state))
  (prev-slide [this state] (protocols/std-prev this state)))

(defrecord ClojureCode [code env pprint-width comment]
  protocols/Slide
  (render-slide [this state]
    [:div {:style.jobim-flexbox (if-not (:css state) flexbox)}
     [:div (when comment
             (if-not (:css state)
               {:style {:margin "2em auto" :max-width "80%"}}
               {:class "jobim-code.commented"}))
      [:div
       (for [[line key] (zipmap code (range (count code)))]
         [:div
          {:key key
           :dangerouslySetInnerHTML
           #js{:__html (str "<pre><code>"
                            (.-value (js/hljs.highlight
                                      "clj"
                                      (with-out-str
                                        (pprint line {:width pprint-width}))))
                            "</code></pre>")}}])]
      (if comment
        [:div
         {:class "jobim-comment"
          :style {:margin-top "2em"}}
         comment]
        [:div])]])
  (next-slide [this state] (protocols/std-next this state))
  (prev-slide [this state] (protocols/std-prev this state)))

(defrecord CustomSlide [component]
  protocols/Slide
  (render-slide [this state] (component state))
  (next-slide [this state] (protocols/std-next this state))
  (prev-slide [this state] (protocols/std-prev this state)))

(defrecord Code [type code-str]
  protocols/Slide
  (render-slide [this state]
    [:pre
     [:code
      {:class type
       :dangerouslySetInnerHTML
       #js{:__html (str "<pre><code>"
                        (.-value (js/hljs.highlight type code-str))
                        "</code></pre>")}}]])
  (next-slide [this state] (protocols/std-next this state))
  (prev-slide [this state] (protocols/std-prev this state)))

(defn render-bullet [css cand-bullet]
  (if (sequential? cand-bullet)
    (into [:ul {:style (if css {} {:font-size "0.8em"})
                :class "jobim-ul"}]
          (map (partial css render-bullet))
          cand-bullet)
    [:li {:class "jobim-li"} cand-bullet]))

(defn render-bullets [css bullets]
  (into [:ul {:class "jobim-ul"}]
        (map (partial render-bullet css)) bullets))

(defrecord BulletedList [title bullets]
  protocols/Slide
  (render-slide [this state]
    [:div
     (when title
       [:h3 {:style (if (:css state) {} {:text-align "center"})
             :class "jobim-list-title"}
        title])
     (render-bullets (:css state) bullets)])
  (next-slide [this state] (protocols/std-next this state))
  (prev-slide [this state] (protocols/std-prev this state)))

(def outer-style
  {:width "100%"
   :height "100%"
   :position "absolute"})

(defn curr-slide [slides state] (nth slides (:page state)))

(defn render-show [slides state show-style]
  [:div.outer-style
   {:style outer-style}
   [:div
    {:style show-style}
    (protocols/render-slide (curr-slide slides state) state)]])

(defn render-show-outer [slides state-atom show-style]
  (render-show slides @state-atom show-style))

(def default-theme
  {:background-color "#222222"
   :color "#EDEDED"
   :font-family "Droid Sans Mono, monospace"
   :font-weight "100"
   :font-size "2em"})

(defn guard-index [state slides]
  (let [slide (curr-slide slides state)]
    (if (satisfies? protocols/Indexable slide)
      (let [n (protocols/max-index slide)
            i (:index state)]
        (cond
          (>= i n) (assoc state :index n)
          (< i 0) (assoc state :index 0)
          :else state))
      state)))

(defn guard-page [state slides]
  (let [n (count slides)
        i (:page state)]
    (cond
      (>= i n) (assoc state :page (dec n))
      (< i 0) (assoc state :page 0)
      :else state)))

(defn guard [state slides]
  (-> state
      (guard-page slides)
      (guard-index slides)))

(defn indent [lv arg]
  (str
   (apply str (take lv (repeat "  ")))
   arg))

(defn indent* [indent-level]
  (fn [arg]
    (if (string? arg)
      (indent indent-level arg)
      (map (indent* (inc indent-level)) arg))))

(defn nl [arg] (str arg "\n"))
(defn nl* [arg]
  (conj (vec (map nl (butlast arg))) (last arg)))

(defn handle-keys [show-state style slides]
  (fn []
   (let [state @show-state
         slide (curr-slide slides state)
         new-state (case (.. js/window -event -keyCode)
                     37 (protocols/prev-slide slide state)
                     38 (if (satisfies? protocols/Indexable slide)
                          (protocols/up-slide slide state)
                          state)
                     39 (protocols/next-slide slide state)
                     40 (if (satisfies? protocols/Indexable slide)
                          (protocols/down-slide slide state)
                          state)
                     state)]
     (reset! show-state (guard new-state slides)))))

(defn render-blog-outer [show-state components]
  (let [state @show-state]
    (into [:div]
     (for [i (range (count components))]
        [:div {:id i}
         [protocols/render-slide (nth components i) state]]))))

(defn slide-show
  [show-state style & slides]
  (when (= :css style) (swap! show-state #(assoc % :css true)))
  (let [input (chan)]
    (set! js/document.onkeydown
          (handle-keys show-state style slides))
    (reagent/render-component
     [render-show-outer slides show-state style]
     (. js/document (getElementById "jobim"))))
  (vec slides))

(defn blog-post
  [show-state & components]
  (swap! show-state #(assoc % :css true))
  (let [components (vec components)]
   (reagent/render-component
    [render-blog-outer show-state components]
    (. js/document (getElementById "jobim")))
   components))

(defn render-inline-code [line]
  [:span.jobim-inline
   {:dangerouslySetInnerHTML
    #js{:__html (str "<code>"
                     (.-value (js/hljs.highlight "clj" line))
                     "</code>")}}])

(defn component [slide]
  (->CustomSlide
   (fn [state]
     [:div.jobim-component
      [(get-in slide [:env :component])]])))
