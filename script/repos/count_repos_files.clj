(ns count-repos-files
  (:require [babashka.fs :as fs]
            [conf :as cnf]))

(defn -main
  [& _args]
  (when (fs/exists? (cnf/repos :root))
    (let [start-time (System/currentTimeMillis)
          exts (atom {})
          n-files (atom 0)]
      (print "Scanning" (cnf/repos :name) "content ... ")
      (flush)
      (fs/walk-file-tree
       (cnf/repos :root)
       {:visit-file
        (fn [path _]
          (swap! n-files inc)
          (swap! exts
                 (fn [old ext-arg]
                   (assoc old
                          ext-arg (inc (get old ext-arg 0))))
                 (fs/extension path))
          :continue)})
      (println "done")
      ;;
      (println "Found" @n-files "files")
      (println "Found" (count @exts) "file extensions")
      ;;
      (println "Clojure-related file extensions")
      (doseq [ext (sort (cnf/repos :extensions))]
        (println ext ":" (get @exts ext 0)))
      (let [n-cljish-files
            (reduce (fn [acc ext]
                      (if-let [cnt (@exts ext)]
                        (+ acc cnt)
                        acc))
                    0
                    (cnf/repos :extensions))]
        (println "Found" n-cljish-files "Clojure-related files"))
      (spit (cnf/repos :file-exts-path) @exts)
      (println "See" (cnf/repos :file-exts-path) "for all extension info.")
      ;;
      (println "Took" (- (System/currentTimeMillis) start-time) "ms"))))

