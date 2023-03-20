(ns deduplicate
  (:import [java.io FileInputStream]
           [java.security MessageDigest])
  (:require [babashka.fs :as fs]
            [conf :as cnf]))

(defn md5
  [file]
  (with-open [f (FileInputStream. file)]
    (let [input (byte-array 4096)
          mdi (MessageDigest/getInstance "MD5")]
      (loop [len (.read f input)]
        (if (pos? len)
          (do
            (.update mdi input 0 len)
            (recur (.read f input)))
          (bigint (.digest mdi)))))))

(def file-checksum md5)

(defn -main
  [& _args]
  (let [start-time (System/currentTimeMillis)
        table (atom {})
        dup-size (atom 0)
        n-files (atom 0)]
    (println "Scanning files ... ")
    (flush)
    (fs/walk-file-tree (cnf/repos :root)
                       {:visit-file
                        (fn [path _]
                          (when (cnf/clojars-extensions (fs/extension path))
                            (let [checksum (file-checksum (fs/file path))]
                              (if-let [original (get @table checksum)]
                                (do
                                  (swap! dup-size + (fs/size path))
                                  (fs/delete path)
                                  (swap! n-files inc))
                                (swap! table assoc checksum path))))
                          :continue)})
    (println "Duplicates found:" @n-files)
    (println "Extra space taken:" @dup-size "bytes")
    (println "Took" (- (System/currentTimeMillis) start-time) "ms")))
