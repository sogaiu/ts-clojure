(ns parse-clojars-code
  (:require [babashka.fs :as fs]
            [babashka.process :as proc]
            [clojure.string :as cs]
            [conf :as cnf]))

(defn -main
  [& _args]
  (when (fs/exists? cnf/clojars-repos-root)
    (let [start-time (System/currentTimeMillis)
          files (atom [])]
      ;; find all relevant clojure-related files
      (print "Looking for files" (sort cnf/clojars-extensions) "... ")
      (flush)
      (fs/walk-file-tree cnf/clojars-repos-root
                         {:visit-file
                          (fn [path _]
                            (when (cnf/clojars-extensions (fs/extension path))
                              (swap! files conj path))
                            :continue)})
      (println "found"
               (count @files) "files"
               "in" (- (System/currentTimeMillis) start-time) "ms")
      ;; save file paths to a file
      (fs/write-lines cnf/clojars-file-paths
                      (map str @files))
      ;; parse with tree-sitter via the paths file
      (print "Invoking tree-sitter to parse files ... ")
      (flush)
      (try
        (let [start-time (System/currentTimeMillis)
              out-file-path (fs/create-temp-file)
              _ (fs/delete-on-exit out-file-path)
              p (proc/process {:dir (cnf/grammar :dir)
                               :out :write
                               :out-file (fs/file out-file-path)}
                              (str cnf/ts-bin-path
                                   " parse --quiet --paths "
                                   cnf/clojars-file-paths))
              exit-code (:exit @p)
              duration (- (System/currentTimeMillis) start-time)]
          (when (= 1 exit-code)
            (println))
          (let [errors (atom 0)]
            ;; save and print error file info
            (fs/write-lines cnf/clojars-error-file-paths
                            (keep (fn [line]
                                    (if-let [[path-ish time message]
                                             (cs/split line #"\t")]
                                      (let [path (cs/trim path-ish)]
                                        (when cnf/verbose
                                          (println message path))
                                        (swap! errors inc)
                                        path)
                                      (println "Did not parse:" line)))
                                  (fs/read-all-lines (fs/file out-file-path))))
            (when (pos? @errors)
              (println "Counted" @errors "paths with parse issues.")
              (println "See" cnf/clojars-error-file-paths
                       "for details or rerun verbosely.")))
          (when-not (#{0 1} exit-code)
            (println "tree-sitter parse exited with unexpected exit-code:"
                     exit-code)
            (System/exit 1))
          (println "Took" duration "ms"))
        (catch Exception e
          (println "Exception:" (.getMessage e))
          (System/exit 1))))))
