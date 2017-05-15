(ns irresponsible.rubdown
  (:require [clojure.spec.alpha :as s]
            [irresponsible.spectra :as ss]
            [clojure.java.io :as io])
  (:import [com.vladsch.flexmark.ast Node]
           [com.vladsch.flexmark.html HtmlRenderer]
           [com.vladsch.flexmark.parser Parser ParserEmulationProfile]
           [com.vladsch.flexmark.util KeepType]
           [com.vladsch.flexmark.util.options MutableDataHolder MutableDataSet]
           [com.vladsch.flexmark.ext.abbreviation AbbreviationExtension]
           [com.vladsch.flexmark.ext.autolink AutolinkExtension]
           [com.vladsch.flexmark.ext.definition DefinitionExtension]
           [com.vladsch.flexmark.ext.footnotes FootnoteExtension]
           [com.vladsch.flexmark.ext.gfm.strikethrough StrikethroughExtension]
           [com.vladsch.flexmark.ext.gfm.tasklist TaskListExtension]
           [com.vladsch.flexmark.ext.tables TablesExtension]
           [com.vladsch.flexmark.ext.typographic TypographicExtension]
           [java.io File Reader]))

;;; extension specs
;; (defmulti ext-spec :extension)
;; (s/def ::extension (s/multi-spec ext-spec ::extension))
(s/def ::extension map?)
;;; making and configuring extensions

(defmulti make-ext
  "Creates an extension from an extension spec map
   args: [ext-map]
   returns: extension"
  (comp :extension); (partial ss/assert! ::extension))
  :default ::default)
(defmulti configure-ext!
  "Configures the options for the given extension
   args: [ext-map opts]
   returns: opts"
  (fn [ext-map opts]
    (:extension ext-map)))

;; helpers
(def keep-type
  {:first  KeepType/FIRST
   :last   KeepType/LAST
   :fail   KeepType/FAIL
   :locked KeepType/LOCKED})
(s/def ::keep-type (set (keys keep-type)))

;;; abbreviation extension
(ss/ns-defs "flexmark.abbreviation"
  :use-links? boolean?)

(s/def :flexmark.ext/abbreviation
  (ss/only-ns-keys "flexmark.abbreviation"
     :req-un [::extension]
     :opt-un [::keep-type :use-links?]))
(defmethod ext-spec :abbreviation  [_] :flexmark.ext/abbreviation)
(defmethod make-ext :abbreviation  [_] (AbbreviationExtension/create))
(defmethod configure-ext! :abbreviation
  [{kt :keep-type :keys [use-links?]} os]
  (when-not (nil? kt)
    (.set os AbbreviationExtension/ABBREVIATIONS_KEEP ^KeepType (keep-type kt)))
  (when-not (nil? use-links?)
    (.set os AbbreviationExtension/USE_LINKS ^Boolean use-links?))
  os)

;;; autolink extension
(s/def :flexmark.ext/autolink      (ss/only-keys :req-un [::extension]))
(defmethod ext-spec :autolink      [_] :flexmark.ext/autolink)
(defmethod make-ext :autolink      [_] (AutolinkExtension/create))
(defmethod configure-ext! :autolink [ext os] os)

;;; definition extension
(ss/ns-defs "flexmark.definition"
  :marker-spaces integer?
  :colon-marker? boolean?
  :tilde-marker? boolean?
  :double-blank-line-breaks-list? boolean?)
(s/def :flexmark.ext/definition
  (ss/only-keys :req-un [::extension]))
(defmethod ext-spec :definition    [_] :flexmark.ext/definition)
(defmethod make-ext :definition    [_] (DefinitionExtension/create))
(defmethod configure-ext! :definition
  [{:keys [marker-spaces colon-marker? tilde-marker? double-blank-line-breaks-list?]}
   os]
  (when-not (nil? marker-spaces)
    (.set os DefinitionExtension/MARKER_SPACES ^Integer marker-spaces))
  (when-not (nil? colon-marker?)
    (.set os DefinitionExtension/COLON_MARKER ^Boolean colon-marker?))
  (when-not (nil? tilde-marker?)
    (.set os DefinitionExtension/TILDE_MARKER ^Boolean tilde-marker?))
  (when-not (nil? double-blank-line-breaks-list?)
    (.set os DefinitionExtension/DOUBLE_BLANK_LINE_BREAKS_LIST ^Boolean double-blank-line-breaks-list?))
  os)

;;; footnote extension
(ss/ns-defs "extmark.footnote"
  :ref-prefix string?
  :ref-suffix string?
  :back-ref-string string?
  :link-ref-class string?
  :back-link-ref-class string?)
(s/def :flexmark.ext/footnote
  (ss/only-ns-keys "flexmark.footnote"
   :req-un [::extension]
   :opt-un [::keep-type
            :ref-prefix
            :ref-suffix
            :back-ref-string
            :link-ref-class
            :back-link-ref-class]))
(defmethod ext-spec :footnote      [_] :flexmark.ext/footnote)
(defmethod make-ext :footnote      [_] (FootnoteExtension/create))
(defmethod configure-ext! :footnote
  [{:keys [keep-type ref-prefix ref-suffix back-ref-string
           link-ref-class back-link-ref-class]}
   os]
  (when-not (nil? keep-type)
    (.set os FootnoteExtension/FOOTNOTES_KEEP ^KeepType keep-type))
  (when-not (nil? ref-prefix)
    (.set os FootnoteExtension/FOOTNOTE_REF_PREFIX ^String ref-prefix))
  (when-not (nil? ref-suffix)
    (.set os FootnoteExtension/FOOTNOTE_REF_SUFFIX ^String ref-suffix))
  (when-not (nil? back-ref-string)
    (.set os FootnoteExtension/FOOTNOTE_BACK_REF_STRING ^String back-ref-string))
  (when-not (nil? link-ref-class)
    (.set os FootnoteExtension/FOOTNOTE_LINK_REF_CLASS ^String link-ref-class))
  (when-not (nil? back-link-ref-class)
    (.set os FootnoteExtension/FOOTNOTE_BACK_LINK_REF_CLASS ^String back-link-ref-class))
  os)

;;; strikethrough extension
(s/def :flexmark.ext/strikethrough (ss/only-keys :req-un [::extension]))
(defmethod ext-spec :strikethrough [_] :flexmark.ext/strikethrough)
(defmethod make-ext :strikethrough [_] (StrikethroughExtension/create))
(defmethod configure-ext! :strikethrough [ext os] os)

;;; tables extension
(ss/ns-defs "flexmark.tables"
  :class-name                     string?
  :min-header-rows                integer?
  :max-header-rows                integer?
  :column-spans?                  boolean?
  :append-missing-columns?        boolean?
  :discard-extra-columns?         boolean
  :with-caption?                  boolean?
  :header-separator-column-match? boolean?
  :trim-cell-whitespace?          boolean?)

(s/def :flexmark.ext/tables
  (ss/only-ns-keys "flexmark.tables"
     :req-un [::extension]
     :opt-un [:min-header-rows :max-header-rows
              :column-spans?   :class-name   :with-caption?
              :append-missing-columns? :header-separator-column-match?
              :discard-extra-columns?  :trim-cell-whitespace?]))
(defmethod ext-spec :tables [_] :flexmark.ext/tables)
(defmethod make-ext :tables [_] (TablesExtension/create))
(defmethod configure-ext! :tables
  [{:keys [min-header-rows max-header-rows
           column-spans? class-name with-caption?
           append-missing-columns? header-separator-column-match?
           discard-extra-columns? trim-cell-whitespace?]}
   os]
  (when-not (nil? min-header-rows)
    (.set os TablesExtension/MIN_HEADER_ROWS (Integer. min-header-rows)))
  (when-not (nil? max-header-rows)
    (.set os TablesExtension/MAX_HEADER_ROWS (Integer. max-header-rows)))
  (when-not (nil? column-spans?)
    (.set os TablesExtension/COLUMN_SPANS ^Boolean column-spans?))
  (when-not (nil? class-name)
    (.set os TablesExtension/CLASS_NAME ^String class-name ))
  (when-not (nil? with-caption?)
    (.set os TablesExtension/WITH_CAPTION ^Boolean with-caption?))
  (when-not (nil? append-missing-columns?)
    (.set os TablesExtension/APPEND_MISSING_COLUMNS ^Boolean append-missing-columns?))
  (when-not (nil? header-separator-column-match?)
    (.set os TablesExtension/HEADER_SEPARATOR_COLUMN_MATCH ^Boolean header-separator-column-match?))
  (when-not (nil? discard-extra-columns?)
    (.set os TablesExtension/DISCARD_EXTRA_COLUMNS ^Boolean discard-extra-columns?))
  (when-not (nil? trim-cell-whitespace?)
    (.set os TablesExtension/TRIM_CELL_WHITESPACE ^Boolean trim-cell-whitespace?))
  os)

;;; tasklist extension
(ss/ns-defs "flexmark.tasklist"
  :item-done-marker string?
  :item-not-done-marker string?
  :item-class string?
  :loose-item-class string?)
(s/def :flexmark.ext/tasklist      (s/keys :req-un [::extension]))
(defmethod ext-spec :tasklist      [_] :flexmark.ext/tasklist) 
(defmethod make-ext :tasklist      [_] (TaskListExtension/create))
(defmethod configure-ext! :tasklist
  [{:keys [item-done-marker item-not-done-marker item-class loose-item-class]}
   os]
  (when-not (nil? item-done-marker)
    (.set os TaskListExtension/ITEM_DONE_MARKER ^String item-done-marker))
  (when-not (nil? item-not-done-marker)
    (.set os TaskListExtension/ITEM_NOT_DONE_MARKER ^String item-not-done-marker))
  (when-not (nil? item-class)
    (.set os TaskListExtension/ITEM_CLASS ^String item-class))
  (when-not (nil? loose-item-class)
    (.set os TaskListExtension/LOOSE_ITEM_CLASS ^String loose-item-class))
  os)
  
;;; typographic extension
(s/def :flexmark.ext/typographic   (s/keys :req-un [::extension]))
(defmethod ext-spec :typographic   [_] :flexmark.ext/typographic)
(defmethod make-ext :typographic   [_] (TypographicExtension/create))
(defmethod configure-ext! :typographic [ext os] os)

(defmulti apply-parser-profile!
  "Configures the options to match the basic parser profiles
   Note that you probably want the 'dialect' multimethod instead!
   args: [name opts]
     name: keyword
     opts: MutableDataSet"
  (fn [name opts] name)
  :default ::default)

(defmethod apply-parser-profile! :commonmark [_ os] os)

(defmethod apply-parser-profile! :kramdown [_ ^MutableDataSet os]
  (.setFrom os ParserEmulationProfile/KRAMDOWN))

(defmethod apply-parser-profile! :github [_ ^MutableDataSet os]
  (.setFrom os ParserEmulationProfile/GITHUB_DOC))

(defmethod apply-parser-profile! :multimarkdown [_ ^MutableDataSet os]
  (.setFrom os ParserEmulationProfile/MULTI_MARKDOWN))

(defmethod apply-parser-profile! :markdown [_ ^MutableDataSet os]
  (.setFrom os ParserEmulationProfile/MARKDOWN))

(defmethod apply-parser-profile! ::default [v os]
  (let [valid (set (keys (methods v)))]
    (->> {:got v :valid valid}
         (ex-info "Unknown parser profile")
         throw)))

(defmulti dialect
  "Returns options compatible with a given dialect
   args: [dialect & [options]]
     dialect: one of :github :multimarkdown :commonmark :kramdown :markdown :multimarkdown
     options: options map, different per dialect
   returns: MutableDataSet"
  (fn [dialect & [_]]
    dialect))

(defn dialect? [d]
  (contains? (set (keys (methods apply-parser-profile!))) d))

(s/def ::extensions (s/coll-of ::extension :into []))
(s/def ::dialect (s/and keyword? dialect?))
(s/def ::make-options (s/keys :opt-un [::dialect ::extensions]))

(defn make-options
  "Configures a MutableDataSet of options according to a map of config data
   args: [opts] ; a map, keys:
     :dialect - a keyword naming a dialect. currently:
       :commonmark :github
     :extensions - a sequence of extension maps. must have ':extension' entry.
       other keys depend on the value of that. currently support extensions:
       :abbreviation
       :autolink
       :definition
       :footnotes
       :strikethrough
       :tables
       :tasklist
    returns: MutableDataSet"
  [opts]
  (let [{:keys [dialect extensions]} opts ;(ss/assert! ::make-options opts)
        os (MutableDataSet.)]
    (apply-parser-profile! dialect os)
    (when (seq extensions)
      (.set os Parser/EXTENSIONS extensions))
    os))

(defmethod dialect :github
  [_ & [opts]]
  (let [exts [{:extension :autolink}
              {:extension :strikethrough}
              {:extension :tables
               :column-spans? false  :min-header-rows 1   :max-header-rows 1
               :with-caption? false  :header-separator-column-match? true
               :append-missing-columns? true  :discard-extra-columns? true}
              {:extension :tasklist}
              ]
        es (map make-ext exts)
        os (make-options {:dialect :github :extensions es})]
    (.set os Parser/REFERENCES_KEEP KeepType/LAST)
    (doseq [e exts]
      (configure-ext! e os))
    os))

;; (defmethod dialect :multimarkdown
;;   [_ & [opts]]
;;   )
;; (defmethod dialect :markdown
;;   [_ & [opts]]
;;   )
;; (defmethod dialect :kramdown
;;   [_ & [opts]]
;;   )

(defmethod dialect :commonmark
  [_ & [opts]]
  )

(defn parser
  "Creates a parser from the given option
   args: [opts] ; MutableDataSet
   returns: Parser"
  [opts]
  (-> opts Parser/builder .build))

(defn parse-string
  "Parses a string with the given Parser
   args: [parser string]
   returns: Document"
  [^Parser p ^String text]
  (.parse p text))

(defn parse-reader
  "Parses a Reader with the given Parser
   args: [parser reader]
   returns: Document"
  [^Parser p ^Reader r]
  (.parseReader p r))

(defn parse-file
  "Parses the file at the given path with the given parser
   args: [parser path]
     parser: Parser
     path:   String
   returns: Document"
  [^Parser p path]
  (.parseReader (io/reader (File. path))))

(defn html-renderer
  "Creates and returns a HtmlRenderer for the given options
   args: [opts] ; MutableDataSet
   returns: HtmlRenderer"
  [opts]
  (-> opts HtmlRenderer/builder .build))

(defn render-html
  "Renders a document to html with the given renderer
   args: [renderer document]
   returns: String"
  [^HtmlRenderer html document]
  (.render html document))
