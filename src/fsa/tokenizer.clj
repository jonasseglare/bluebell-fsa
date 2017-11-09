(ns fsa.graphviz)

(defn remove-eol-comment [s]
  (let [i (.indexOf s "//")]
    (if (= -1 i)
      s
      (subs s 0 i))))

(defn parse [x]
  (apply
   str
   (map remove-eol-comment
        (clojure.string/split-lines x))))
