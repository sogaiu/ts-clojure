(ns prune
  (:require [babashka.fs :as fs]
            [conf :as cnf]))

(defn -main
  [& _args]
  (let [start-time (System/currentTimeMillis)
        rm-size (atom 0)
        n-files (atom 0)]
    (println "Pruning files without following extensions"
             (sort (cnf/repos :extensions)) "... ")
    (flush)
    (fs/walk-file-tree
     (cnf/repos :root)
     {:visit-file
      (fn [path _]
        (when-not ((cnf/repos :extensions) (fs/extension path))
          (swap! rm-size + (fs/size path))
          (fs/delete path)
          (swap! n-files inc))
        :continue)})
    (println "Files removed:" @n-files)
    (println "Space reclaimed:" @rm-size "bytes")
    (println "Took" (- (System/currentTimeMillis) start-time) "ms")))
