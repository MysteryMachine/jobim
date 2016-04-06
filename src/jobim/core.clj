(ns jobim.core)

(def end :JOBIM_SPECIAL_END)

(defn def-form? [form] (= (first form) 'def))
(defn defn-form? [form] (= (first form) 'defn))

(defn def-form [[_ name & code]]
  `[~name ~@code])

(defn defn-form [[_ name args & code]]
  `[~name (fn ~name ~args ~@code)])

(defn nameless-form [length code]
  `[~(symbol (str "%" length)) ~code])

(defn form [length code]
  (cond
    (not (seq? code)) (nameless-form length code)
    (def-form? code)  (def-form code)
    (defn-form? code) (defn-form code)
    :else (nameless-form length code)))

(defn expand-to-form [{:keys [length forms] :as env} code]
  (-> env
      (assoc-in [:forms] (conj forms (form length code)))
      (update-in [:length] inc)))

(defn key-pair [[sym & _]] [(keyword sym) sym])

(defn build-forms [code] (:forms (reduce expand-to-form {:length 0 :forms []} code)))

(defn transform-code [code]
  (let [forms (build-forms code)]
    `(let ~(vec (reduce concat forms))
       ~(apply hash-map (reduce concat (map key-pair forms))))))

(defmacro clojure-code [width & code]
  `(->ClojureCode '~code ~(transform-code code) ~width))

(defmacro defshow [name state style & slides]
  `(def ~name (slide-show ~state ~style ~@slides)))

(defmacro defclj [name & code]
  `(def ~name (clojure-code ~@code)))

(defmacro pseudo-clj [width & code]
  `(->ClojureCode '~code {:length 0} ~width))

;; Helpers

(defmacro ->p [& words]
  `[:p (clojure.string/join " " [~@words])])

(defmacro ->style [style & more]
  `[:div {:style ~style} ~@more])
