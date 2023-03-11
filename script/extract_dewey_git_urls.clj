(ns extract-dewey-git-urls
  (:require [babashka.fs :as fs]
            [babashka.process :as proc]
            [clojure.edn :as ce]
            [conf :as cnf]))

(defn -main
  [& _args]
  (when (fs/exists? cnf/dewey-all-repos-edn)
    (println "Extracting git repos urls from all-repos.edn...")
    (let [start-time (System/currentTimeMillis)
          out-file-path (fs/create-temp-file)]
      (fs/delete-on-exit out-file-path)
      (fs/write-lines out-file-path
                      (keep :html_url
                            (ce/read-string
                             (slurp (fs/file cnf/dewey-all-repos-edn)))))
      ;; XXX: not cross-platform?
      (proc/process "sort" "--output" cnf/dewey-git-repos-urls out-file-path)
      (println "Took" (- (System/currentTimeMillis) start-time) "ms"))))
