(ns jobim.core
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [put! chan >! <!]]
            [fipp.clojure :refer [pprint]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defprotocol Slide
  (render-slide [this state])
  (next-slide   [this state])
  (prev-slide   [this state]))

(defn std-next [this state] (update-in state [:page] inc))
(defn std-prev [this state] (update-in state [:page] dec))

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
  Slide
  (render-slide [this state]
    [:div
     {:style title-style}
     (center 80
       [:h1
        {:style h1-style}
        title])
     (center 66.6
      [:h2
       {:style h2-style}
       subtitle])])
  (next-slide [this state] (std-next this state))
  (prev-slide [this state] (std-prev this state)))

(defrecord Text [text]
  Slide
  (render-slide [this state]
    [:div {:style title-style}
     (center 80
       [:h1 {:style h1-style} text])])
  (next-slide [this state] (std-next this state))
  (prev-slide [this state] (std-prev this state)))

(def pic-style
  {:height "100%"
   :width "auto"
   :outline "10px #0E0E0E solid"})

(defrecord Picture [url]
  Slide
  (render-slide [this state]
    [:div
     {:style (merge flexbox {:height "50%" :width "50%"})}
     [:img {:src url :style pic-style}]])
  (next-slide [this state] (std-next this state))
  (prev-slide [this state] (std-prev this state)))

(defrecord CaptionedPic [url caption]
  Slide
  (render-slide [this state]
    [:div {:style (merge flexbox {:flex-direction "column"})}
     (render-slide (->Picture url) state)
     (center 80
       [:div
        {:style {:padding-top "50px" :text-align "center"}}
        caption])])
  (next-slide [this state] (std-next this state))
  (prev-slide [this state] (std-prev this state)))

(defrecord ClojureCode [code env pprint-width comment]
  Slide
  (render-slide [this state]
    [:div {:style flexbox}
     [:div (when comment
             {:style {:margin "2em auto"
                      :max-width "80%"}})
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
         {:class "comment"
          :style {:margin-top "2em"}}
         comment]
        [:div])]])
  (next-slide [this state] (std-next this state))
  (prev-slide [this state] (std-prev this state)))

(defrecord CustomSlide [component]
  Slide
  (render-slide [this state] (component state))
  (next-slide [this state] (std-next this state))
  (prev-slide [this state] (std-prev this state)))

(defrecord Code [type code-str]
  Slide
  (render-slide [this state]
    [:pre
     [:code
      {:class type
       :dangerouslySetInnerHTML
       #js{:__html (str "<pre><code>"
                        (.-value (js/hljs.highlight type code-str))
                        "</code></pre>")}}]])
  (next-slide [this state] (std-next this state))
  (prev-slide [this state] (std-prev this state)))

(defn- render-bullet [cand-bullet]
  (if (sequential? cand-bullet)
    (into [:ul {:style {:font-size "0.8em"}}] (map render-bullet cand-bullet))
    [:li cand-bullet]))

(defn render-bullets [bullets]
  (into [:ul] (map render-bullet bullets)))

(defrecord BulletedList [title bullets]
  Slide
  (render-slide [this state]
    [:div
     [:h3 {:style {:text-align "center"}} title]
     (render-bullets bullets)])
  (next-slide [this state] (std-next this state))
  (prev-slide [this state] (std-prev this state)))

(def outer-style
  {:width "100%"
   :height "100%"
   :position "absolute"})

(defn curr-slide [slides state] (nth slides (:page state)))

(defn render-show [slides state show-style]
  [:div
   {:style outer-style}
   [:div
    {:style show-style}
    (render-slide (curr-slide slides state) state)]])

(defn render-show-outer [slides state-atom show-style]
  (render-show slides @state-atom show-style))

(def default-theme
  {:background-color "#222222"
   :color "#EDEDED"
   :font-family "Droid Sans Mono, monospace"
   :font-weight "100"
   :font-size "2em"})

(def default-style
  (merge flexbox
         default-theme))

(defn guard [state slides]
  (let [n (count slides)
        i (:page state)]
    (cond
      (>= i n) (assoc-in state [:page] (dec n))
      (< i 0) (assoc-in state [:page] 0)
      :else state)))

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

(defn code [type & code]
  (->> code
       (map (indent* 0))
       (flatten)
       (nl*)
       (apply str)
       (->Code type)))

(defn slide-show [show-state style & slides]
  (let [input (chan)]
    (set! js/document.onkeydown
     (fn []
       (let [state @show-state
             new-state (case (.. js/window -event -keyCode)
                         37 (prev-slide (curr-slide slides state) state)
                         39 (next-slide (curr-slide slides state) state)
                         state)]
         (reset! show-state (guard new-state slides)))))
    (reagent/render-component
     [render-show-outer slides show-state style]
     (. js/document (getElementById "jobim"))))
  (vec slides))

(defn new-show [] (atom {:page 0}))
