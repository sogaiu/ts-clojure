(ns check-setup
  (:require [babashka.fs :as fs]
            [babashka.process :as proc]
            [clojure.string :as cs]
            [utils :as u]
            [conf :as cnf]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def count-samples? (atom true))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn which
  [name]
  ;; XXX: probably a better way...
  (when-let [path (fs/which name)]
    (format "%s" (fs/which name))))

(def prereq-paths
  {:tree-sitter (which cnf/ts-bin-path)
   :git (which "git")
   :cc (which "cc")
   :node (which "node")})

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
    (u/exit-unless
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn print-separator
  []
  (println "------------------------------------------------------------------"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn check-prereq-paths
  [state]
  (merge state prereq-paths))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn report-prereq-paths
  [state]
  (println "prerequisities")
  (let [ts (get state :tree-sitter)
        git (get state :git)
        cc (get state :cc)
        node (get state :node)]
    (u/exit-unless ts "tree-sitter not found")
    (println "  tree-sitter:" ts)
    (u/exit-unless git "git not found")
    (println "          git:" git)
    (u/exit-unless cc "cc not found")
    (println "           cc:" cc)
    (u/exit-unless node "node not found")
    (println "         node:" node)
    ;;
    (print-separator)
    ;;
    state))

(defn report-grammar-dir
  [state]
  (println "grammar-dir")
  (println "        directory set to:" cnf/grammar-dir)
  (let [exists (get state :grammar-dir-exists)]
    (u/exit-unless exists
                   (format "grammar-dir (%s) does not exist"
                           cnf/grammar-dir))
    (println "        directory exists: Yes"))
  (let [parser-visible (get state :tree-sitter-sees-parser)]
    (u/exit-unless parser-visible
                   (format "tree-sitter did not find grammar-dir (%s)"
                           cnf/grammar-dir))
    (println "  visible to tree-sitter: Yes"))
  ;;
  (print-separator)
  ;;
  state)

(defn report-abi
  [state]
  (println "abi number")
  (let [abi-is-number (get state :abi-is-number)]
    (u/exit-unless abi-is-number
                   (format "abi was not a number: %s %s"
                           cnf/abi (type cnf/abi)))
    (println "  abi:" cnf/abi))
  ;;
  (print-separator)
  ;;
  state)

(defn report-repos
  [state]
  (println "samples")
  (println "     samples repos:" (cnf/repos :name))
  (println "  directory set to:" (cnf/repos :root))
  (let [exists (get state :repos-root-exists)]
    (u/exit-unless exists
                   (format "repos (%s) does not exist"
                           (cnf/repos :root)))
    (println "  directory exists: Yes")
    (when (and exists
               @count-samples?)
      (println "[Counting samples too...this might take a while.]")
      (println "[Hint: Invoke with -1 as argument to skip sample counting.]")
      (println "       # of samples:" (count (u/collect-samples)))))
  ;;
  (print-separator)
  ;;
  state)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; outline:
;;
;; * report paths of tree-sitter, git, c compiler, and node
;; * report path of tree-sitter-clojure found by tree-sitter
;; * report path of grammar-dir from conf (should match above)
;; * report abi number from conf
;; * report current repos setting
;; * report paths of samples for current grammar
(defn check-and-report-findings
  []
  (-> {}
      check-prereq-paths
      report-prereq-paths
      ;;
      check-grammar-dir
      report-grammar-dir
      ;;
      check-abi
      report-abi
      ;;
      check-repos
      report-repos)
  ;;
  (println "Setup looks ok."))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn -main
  [& _args]
  (println "ts-clojure: checking setup...")
  (print-separator)
  ;;
  (when (= "-1" (first *command-line-args*))
    (reset! count-samples? false))
  ;;
  (try
    (check-and-report-findings)
    (catch Exception e
      (u/report-exception-and-exit e))))

