;;; classify-parse-errors --- classify tree-sitter parse errors

;;; Commentary:

(require 'dired-x)

;;; Code:

(defvar cpe-proj-root
  ;; XXX: is this good enough?
  (file-name-directory (or load-file-name (buffer-file-name))))

(defvar cpe-data-dir-name
  "data")

(defvar cpe-file-path
  (concat cpe-proj-root cpe-data-dir-name "/classify-parse-errors.tsv"))

(defvar cpe-error-files-default-name
  "clojars-error-files.txt")

(defun cpe-new-session (path)
  "Start a new session.

Optional argument PATH specifies a path to a file with paths in it."
  (interactive
   (list (read-file-name "Errors file name: "
                         (concat cpe-data-dir-name "/")
                         nil
                         t
                         cpe-error-files-default-name)))
  (find-file path)
  (shell-command-on-region (point-min) (point-max)
                           "xargs ls -al")
  (switch-to-buffer "*Shell Command Output*")
  ;; XXX: because atm all of the file paths are absolute
  (dired-virtual "/"))

(defun cpe-append-entry (reason)
  "Append a new entry with REASON."
  ;; XXX: read in past reasons from cpe-file-path?
  ;; XXX: should prune tab characters from reason
  (interactive "sReason: ")
  ;; XXX
  (message "%s" reason)
  (let ((file-name (dired-get-filename)))
    (when file-name
      ;; XXX
      (message "file name: %s" file-name)
      (let ((checksum (md5 file-name)))
        ;; XXX
        (message "md5: %s" checksum)
        (append-to-file (concat checksum "\t"
                                reason "\t"
                                ;; XXX: may be want jar / zip url?
                                file-name "\n")
                        nil cpe-file-path)))))

(provide 'classify-parse-errors)
;;; classify-parse-errors.el ends here
