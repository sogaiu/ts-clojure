(ns utils
  (:require [babashka.fs :as fs]
            [babashka.process :as proc]
            [clojure.string :as cs]
            [conf :as cnf]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn valid-repos?
  [repos]
  (and (map? repos)
       (contains? repos :name)
       (contains? repos :root)
       (contains? repos :extensions)
       (contains? repos :error-file-paths)))

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

(defn exit-unless
  [condition message]
  (when-not condition
    (println message)
    (System/exit 1)))

(defn report-exception-and-exit
  [e]
  (println "Exception:" e)
  (System/exit 1))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn check-prereq-paths
  [state]
  (let [which (fn [name]
                ;; XXX: probably a better way...
                (when-let [path (fs/which name)]
                  (format "%s" (fs/which name))))]
    (merge state {:tree-sitter (which cnf/ts-bin-path)
                  :git (which "git")
                  :cc (which "cc")
                  :node (which "node")})))

;; sample output from tree-sitter dump-languages
;;
;;   scope: source.janet
;;   parser: "./tree-sitter-janet-simple/"
;;   highlights: None
;;   file_types: ["cgen", "janet", "jdn"]
;;   content_regex: None
;;   injection_regex: None
;;
;;   scope: source.clojure
;;   parser: "./tree-sitter-clojure/"
;;   highlights: None
;;   file_types: ["bb", "clj", "cljc", "cljs"]
;;   content_regex: None
;;   injection_regex: None
;;
(defn parsers-from-dump-languages
  []
  (let [out-file-path (fs/create-temp-file)
        _ (fs/delete-on-exit out-file-path)
        p (proc/process {:out :write
                         :out-file (fs/file out-file-path)}
                        (str cnf/ts-bin-path " dump-languages"))
        exit-code (:exit @p)]
    (exit-unless
     (zero? exit-code)
     (format "tree-sitter dump-languages exited non-zero: %d"
             exit-code))
    (keep (fn [line]
            (when (pos? (count line))
              (let [[name value] (cs/split line #": ")]
                (when (= name "parser")
                  (let [no-quotes (subs value 1 (dec (count value)))]
                    no-quotes)))))
          (fs/read-all-lines (fs/file out-file-path)))))

(defn tree-sitter-sees-parser?
  []
  (when (fs/exists? cnf/grammar-dir)
    (loop [parsers (parsers-from-dump-languages)]
      (cond
        (empty? parsers)
        false
        ;;
        (fs/same-file? (first parsers) cnf/grammar-dir)
        true
        ;;
        :default
        (recur (rest parsers))))))

(defn check-grammar-dir
  [state]
  (-> state
      (merge {:grammar-dir-exists (fs/exists? cnf/grammar-dir)})
      (merge {:tree-sitter-sees-parser (tree-sitter-sees-parser?)})))

(defn check-abi
  [state]
  (merge state {:abi-is-number (number? cnf/abi)}))

(defn check-repos
  [state]
  (merge state {:repos-root-exists (fs/exists? (cnf/repos :root))}))

