(ns jobim.core
  "These functions are almost all you need to use Jobim to its
   fullest potential. While reading this document, it is important
   to note a few conventions utilized within.

   <hiccup>: Whenever a function is said to take hiccup, this means
   it can take either a string, or a hiccup style html data
   structure. Jobim allows you to fully customize your text with
   additional markup at any time.

   CSS: These sections indicate classes you can use to style your
   slide from within CSS.

   If you are looking to create your own custom slides, check out
   `jobim.protocols` for more information.

   Be sure to read the macros in the clj `jobim.core` namespace."
  (:require [reagent.core :as reagent]
            [jobim.core.impl :as impl]))

(defn title
  "A function for creating title slides.
   <title>: hiccup for the title text
   <subtitle>: hiccup for the subtitle

   CSS: jobim-title, jobim-subtitle"
  [title subtitle]
  (impl/->Title title subtitle))

(defn text
  "A function for creating slides with just text.
   <text>: hiccup for your slide's text

   CSS: jobim-text"
  [text]
  (impl/->Text text))

(defn img
  "A function for creating a slide with just an image.
   <src>: a string indicating the image's source"
  [src]
  (impl/->Picture src))

(defn captioned-img
  "A function for creating a slide with just an image.
   <src>: a string indicating the image's source
   <caption>: hiccup indicating a caption

   CSS: jobim-caption"
  [src caption]
  (impl/->CaptionedPic src caption))

(defn bullets
  "Given a title and some buellets, draw a bulleted list.
   <title>: hiccup denoting a title
   <bullets>: any number of hiccup denoting bullets

   CSS: jobim-ul, jobim-li, jobim-list-title"
  [title & bullets]
  (impl/->BulletedList title bullets))

(defn custom-slide
  "Given a reagent component, render it as a slide.
   <component>: A zero argument function that returns a valid
   reagent data structure."
  [component]
  (impl/->CustomSlide component))

(def default-style
  "Jobim's default style. I recommend starting from here, and
   merging any other styles you might want."
  (merge impl/flexbox impl/default-theme))

(defn new-show
  "A helper function for creating a properly initialized
   Jobim state."
  []
  (reagent/atom {:page 0}))

(defn curr-slide
  "Access the current slide of the show.
   <slides>: A jobim slide show
   <state>: An atom representing the current state of the
   slide show."
  [slides state]
  (impl/curr-slide slides state))

(defn code*
  "Used for creating simple code examples in other languages.
   <type>: The type of code this is, eg, Python, or Javascript.
   <code>: The code data structure.

   In this function, you nest code in brackets to indicate indendation.
   For example,

   (*code \"js\"
     [\"function plus(a, b){\"
      [\"return a + b\";]
      \"}\"])

   would return the following Javascript:

   function plus(a,b){
     return a + b;
   }

   It is important to note Jobim does not actually run this code.
   It is simply text example."
  [type & code]
  (->> code
       (map (impl/indent* 0))
       (flatten)
       (impl/nl*)
       (apply str)
       (impl/->Code type)))
