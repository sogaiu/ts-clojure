(ns check-js-bits
  (:require [babashka.fs :as fs]))

(defn -main
  [& _args]
  (when-not (fs/which "node")
    (println "Did not find node.")
    (System/exit 1)))
