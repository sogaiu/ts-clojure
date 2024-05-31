(ns utils
  (:require [babashka.fs :as fs]
            [conf :as cnf]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn collect-samples
  []
  (let [samples (atom [])]
    (fs/walk-file-tree
     (cnf/repos :root)
     {:visit-file
      (fn [path _]
        (when ((cnf/repos :extensions) (fs/extension path))
          (swap! samples conj path))
        :continue)
      :follow-links true})
    @samples))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn exit-unless-repos-root-exists
  []
  (when-not (fs/exists? (cnf/repos :root))
    (println "Directory for" (cnf/repos :root) "not found")
    (System/exit 1)))

(defn exit-unless-grammar-dir-exists
  []
  (when-not (fs/exists? cnf/grammar-dir)
    (println "Directory for" cnf/grammar-dir "not found")
    (System/exit 1)))

(defn exit-unless-error-code-is
  [exit-code code-set cmd-str]
  (when-not (code-set exit-code)
    (println cmd-str "exited with unexpected exit-code:"
             exit-code)
    (System/exit 1)))

(defn report-exception-and-exit
  [e]
  (println "Exception:" e)
  (System/exit 1))
