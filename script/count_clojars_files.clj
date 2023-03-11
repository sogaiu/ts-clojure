(ns count-clojars-files
  (:require [babashka.fs :as fs]
            [babashka.process :as proc]
            [clojure.java.io :as cji]
            [conf :as cnf]))

(defn -main
  [& _args]
  (when (fs/exists? cnf/clojars-repos-root)
    (let [start-time (System/currentTimeMillis)
          exts (atom {})]
      (print "Scanning repositories ... ")
      (fs/walk-file-tree cnf/clojars-repos-root
                         {:visit-file
                          (fn [path _]
                            (swap! exts
                                   (fn [old ext-arg]
                                     (assoc old
                                            ext-arg (inc (get old ext-arg 0))))
                                   (fs/extension path))
                            :continue)})
      (println "found" (count @exts) "file extensions")
      (doseq [ext (sort cnf/clojars-extensions)]
        (println ext ":" (get @exts ext 0)))
      (spit cnf/clojars-file-exts-path @exts)
      (println "Took" (- (System/currentTimeMillis) start-time) "ms"))))


