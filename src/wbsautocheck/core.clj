(ns wbsautocheck.core)

(use 'dk.ative.docjure.spreadsheet) ; docjure


;;################################
;;WBS Excelを逐次読み込み処理
;;################################
(defn wbs2json
  [workbookfilename targetsheetname]
  (do
    (def ckou1 0)
    (def ckou2 0)
    (def ckou3 0)
    (def ckou4 0)
  (doseq [r1 (->> (load-workbook workbookfilename)
       (select-sheet targetsheetname)
       (select-columns {:A :kou1, :B :kou2, :C :kou3, :D :kou4, :E :taskname1, :F :taskname2, :G :taskname3, :H :taskname4, :I :yo-st, :J :yo-en, :K :ji-st, :L :ji-en :M :tantou})
       rest
       rest
       rest
       rest
       rest
       rest
       rest)] ;;頭7行スキップ
    (do
      (if (not= (:kou1 r1) nil) (def ckou1 (:kou1 r1)))
      (if (not= (:kou2 r1) nil) (def ckou2 (:kou2 r1)))
      (if (not= (:kou3 r1) nil) (def ckou3 (:kou3 r1)))
      (if (not= (:kou4 r1) nil) (def ckou4 (:kou4 r1)))
      (if (and (not= (:yo-st r1) nil) (not= (:yo-en r1) nil)
               (= (.toString (type (:yo-st r1))) "class java.util.Date")
               (= (.toString (type (:yo-en r1))) "class java.util.Date"))
        (printf "%d.%d.%d.%d: %s yo-st=%s yo-en=%s tantou=%s\n"
                (int ckou1)
                (int ckou2)
                (int ckou3)
                (int ckou4)
                (str (:taskname1 r1) (:taskname2 r1) (:taskname3 r1) (:taskname4 r1))
                (.format (java.text.SimpleDateFormat. "yyyy/MM/dd") (:yo-st r1))
                (.format (java.text.SimpleDateFormat. "yyyy/MM/dd") (:yo-en r1))
                (:tantou r1))
        )
        ;;(printf "kou1:%s kou2:%s kou3:%s taskname:%s\n" (:kou1 r1) (:kou2 r1) (:kou3 r1) (:taskname2 r1))
      );;end-of-do
    );;end-of-doseq
  );end-of-top do
  );;end-of-defn

;;################################
;;main
;;lein run wbsautocheck <引数1>
;;<引数1> : WBS Excelのフルパス
;;<引数2> : WBS Excelのシート名
;;################################
(defn -main
  [& args]
  (do
    (println "WBS Excelファイルのフルパス:" (first args))
    (println "WBS Excelのシート名:" (second args))
    (if (and (.exists (clojure.java.io/as-file (first args)))
             (not= (second args) nil))
      ;;引数指定したファイル存在する。ここから処理本体
      (do
        (println "指定したExcelファイル存在します。")
        (wbsautocheck.core/wbs2json (first args) (second args)))
      ;;引数が不正
      (println "引数指定したファイルが存在しません。")
      )))
