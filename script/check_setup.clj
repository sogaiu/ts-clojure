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
  (format "%s" (fs/which name)))

(def prereq-paths
  {:tree-sitter (which cnf/ts-bin-path)
   :git (which "git")
   :cc (which "cc")
   :node (which "node")})

(defn absolute-path
  [path]
  ;; XXX: probably a better way...
  (format "%s" (fs/absolutize (fs/normalize (fs/file path)))))

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

(defn report-prereq-paths
  []
  (let [ts (get prereq-paths :tree-sitter)
        git (get prereq-paths :git)
        cc (get prereq-paths :cc)
        node (get prereq-paths :node)]
    (println "Checking prerequisities...")
    (u/exit-unless ts "tree-sitter not found")
    (u/exit-unless git "git not found")
    (u/exit-unless cc "cc not found")
    (u/exit-unless node "node not found")
    ;;
    (println "tree-sitter:" ts)
    (println "        git:" git)
    (println "         cc:" cc)
    (println "       node:" node)))

(defn report-grammar-dir
  []
  (println "Checking grammar-dir...")
  (let [exists (fs/exists? cnf/grammar-dir)]
    (println "           grammar-dir set to:" cnf/grammar-dir)
    (u/exit-unless exists
                   (format "grammar-dir (%s) does not exist"
                           cnf/grammar-dir))
    (println "           grammar-dir exists: Yes"))
  (let [parsers (parsers-from-dump-languages)]
    ;; XXX: probably there's a better way to do this
    (let [found (atom false)]
      (doseq [p parsers]
        (when (= (absolute-path p)
                 (absolute-path cnf/grammar-dir))
          (reset! found true)))
      (u/exit-unless @found
                     (format "tree-sitter did not find grammar-dir (%s)"
                             cnf/grammar-dir))
      (println "tree-sitter found grammar-dir: Yes"))))

(defn report-abi
  []
  (println "Checking ABI setting...")
  (u/exit-unless (number? cnf/abi)
                 (format "abi was not a number: %s %s"
                         cnf/abi (type cnf/abi)))
  (println "abi:" cnf/abi))

(defn report-repos
  []
  (println "Checking sample repos...")
  (let [exists (fs/exists? (cnf/repos :root))]
    (println "      samples repos:" (cnf/repos :name))
    (println "samples root set to:" (cnf/repos :root))
    (u/exit-unless exists
                   (format "repos (%s) does not exist"
                           (cnf/repos :root)))
    (println "samples root exists: Yes")
    (when (and exists
               @count-samples?)
      (println "[Counting samples too...this might take a while.]")
      (println "[Hint: Invoke with -1 as argument to skip sample counting.]")
      (println "       # of samples:" (count (u/collect-samples))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; outline:
;;
;; * report paths of tree-sitter, git, c compiler, and node
;; * report path of tree-sitter-clojure found by tree-sitter
;; * report path of grammar-dir from conf (should match above)
;; * report abi number from conf
;; * report current repos setting
;; * report paths of samples for current grammar
(defn -main
  [& _args]
  (println "ts-clojure: checking setup")
  (print-separator)
  ;;
  (when (= "-1" (first *command-line-args*))
    (reset! count-samples? false))
  ;;
  (try
    (let [_ (report-prereq-paths)
          _ (print-separator)
          _ (report-grammar-dir)
          _ (print-separator)
          _ (report-abi)
          _ (print-separator)
          _ (report-repos)
          _ (print-separator)]
      (println "Setup looks ok."))
    (catch Exception e
      (u/report-exception-and-exit e))))

