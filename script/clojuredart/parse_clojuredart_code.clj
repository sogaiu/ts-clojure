(ns parse-clojuredart-code
  (:require [babashka.fs :as fs]
            [babashka.process :as proc]
            [clojure.string :as cs]
            [conf :as cnf]))

(defn -main
  [& _args]
  (when (fs/exists? cnf/cljd-repos-root)
    (let [start-time (System/currentTimeMillis)
          files (atom [])
          paths-file (fs/create-temp-file)
          _ (fs/delete-on-exit paths-file)]
      ;; find all .cljd files
      (fs/walk-file-tree cnf/cljd-repos-root
                         {:visit-file
                          (fn [path _]
                            (when (cnf/cljd-extensions (fs/extension path))
                              (swap! files conj path))
                            :continue)})
      (println (sort cnf/cljd-extensions) "files found:" (count @files))
      ;; save .cljd file paths to a file
      (fs/write-lines paths-file
                      (map str @files))
      ;; parse via the paths file
      (try
        (proc/process {:dir (cnf/grammar :dir)}
                      (str cnf/ts-bin-path " parse --quiet --paths " paths-file))
        (catch Exception e
          (println "Exception:" (.getMessage e))))
      (println "Took" (- (System/currentTimeMillis) start-time) "ms"))))
