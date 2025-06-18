(ns dump-target-file-paths
  (:require [babashka.fs :as fs]
            [clojure.java.io :as cji]
            [clojure.string :as cs]
            [conf :as cnf]
            [utils :as u]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn save-sample-paths
  [samples]
  (let [to-be-parsed (fs/create-temp-file)]
    ;; XXX
    ;;(fs/delete-on-exit to-be-parsed)
    (fs/write-lines to-be-parsed (map str samples))
    to-be-parsed))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn report-looking
  []
  (println "Looking in samples collection:" (cnf/repos :name))
  (print "Focusing on" (sort (cnf/repos :extensions))
         "files ... ")
  (flush))

(defn report-found
  [samples start-time]
  (println "found"
           (count samples) "files"
           "in" (- (System/currentTimeMillis) start-time) "ms"))

(defn report-file-path
  [path]
  (println "File paths saved in:")
  ;; XXX: may be there's a better way?
  (println "  " (.toString path)))

(defn report-duration
  [duration]
  (println "Took" duration "ms"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; outline:
;;
;; 1. collect files to parse
;; 2. save file paths to file
;; 3. output file path
(defn -main
  [& args]
  (u/exit-unless-grammar-dir-exists)
  ;;
  (println "Determining samples")
  (let [repos (first args)]
    ;; convenience for setting samples set to test against
    (when repos
      ;; XXX: may be there's a better way to do this?
      (if-let [repos-var (find-var (symbol (str "conf/" repos)))]
        (do
          (u/exit-unless-valid-repos @repos-var)
          ;; https://stackoverflow.com/a/10987054
          (alter-var-root #'cnf/repos (constantly repos-var)))
        (do
          (println "Did not find samples repos with name:" repos)
          (System/exit 1))))
    ;;
    (u/exit-unless-repos-root-exists)
    ;;
    (try
      (let [start-time (System/currentTimeMillis)
            _ (report-looking)
            ;; 1. find all relevant clojure-related files
            samples (u/collect-samples)
            _ (report-found samples start-time)
            ;; 2. save file paths to be parsed to a file
            to-be-parsed (save-sample-paths samples)
            ;; 3. output file path
            _ (report-file-path to-be-parsed)
            duration (- (System/currentTimeMillis) start-time)]
        (report-duration duration))
      (catch Exception e
        (u/report-exception-and-exit e)))))

