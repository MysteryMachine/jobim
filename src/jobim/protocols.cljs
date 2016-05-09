(ns jobim.protocols
  "Jobim slides are built on a simple protocol, and can be
   extended to take advantage of another one. Default implementations
   of these protocols are available for you to use, in case the
   very custom behavior is not really desired, and you just want to
   define a way to rener the Slide.")

(defprotocol Slide
  "The base abstraction of Jobim, defines a slide an its interface.
   `next-slide` and `prev-slide` should be idempotent in a correct
   implementation, and should alter the :page value on the state passed
   in to them. An important thing to note in these implementations
   is that Jobim guards for overflow for you, so these functions do
   not have to worry. Additionally, `next-slide` and `prev-slide`
   should clean up any other state assoced into your slide's global
   state."
  (render-slide
   [this state]
   "Should return a Reagent data structure.")
  (next-slide
   [this state]
   "Given a state, should return a new state that moves the slide to
    the next page, or, optionally, the current slide to its next state.
    Triggers when pressing right.")
  (prev-slide
   [this state]
   "Given a state, should return a new state that moves the slide to
    the prev page, or, optionally, the current slide to its prev state.
    Triggers when pressing left."))

(defprotocol Indexable
  "This abstraction lets you hook into an up and down key. These keys should
   alter the the :index key of the state passed in. Jobim guards this value
   from overflow by calling `max-index`, and by default, does not allow you
   to specify negative indicies."
  (up-slide
   [this state]
   "Given a state, should return a new state that moves the slide to
    its next state. Triggers when pressing up.")
  (down-slide
   [this state]
   "Given a state, should return a new state that moves the slide to
    its prev state. Triggers when pressing down.")
  (max-index
   [this]
   "Slides implementing this protocol should know the max index possible."))

(defn std-next
  "A default implementation of `next-slide`"
  [this state]
  (-> state
      (update :page  inc)
      (assoc  :index 0)))

(defn std-prev
  "A default implementation of `prev-slide`"
  [this state]
  (-> state
      (update :page  dec)
      (assoc  :index 0)))

(defn std-up
  "A default implementation of `up-slide`"
  [this state]
  (update state :index inc))

(defn std-down
  "A default implementation of `down-slide`"
  [this state]
  (update state :index dec))
