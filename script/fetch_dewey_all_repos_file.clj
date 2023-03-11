(ns fetch-dewey-all-repos-file
  (:require [babashka.fs :as fs]
            [babashka.process :as proc]
            [conf :as cnf]))

(defn -main
  [& _args]
  (let [gzipped-path (str cnf/dewey-all-repos-edn ".gz")
        start-time (System/currentTimeMillis)]
    (when (not (fs/exists? cnf/dewey-all-repos-edn))
      (println "Fetching all-repos.edn.gz...")
      (let [p (proc/process "curl"
                            cnf/dewey-all-repos-edn-url
                            "--location"
                            "--output" gzipped-path)
            exit-code (:exit @p)]
        (when-not (zero? exit-code)
          (println "Problem fetching all-repos.edn")
          (System/exit 1))))
    (when (fs/exists? gzipped-path)
      (println "Uncompressing all-repos.edn.gz...")
      (let [p (proc/process "gzip" "--decompress" gzipped-path)
            exit-code (:exit @p)]
        (when-not (zero? exit-code)
          (println "Problem uncompressing all-repos.edn.gz")
          (System/exit 1))
        (println "Took" (- (System/currentTimeMillis) start-time) "ms")))))

