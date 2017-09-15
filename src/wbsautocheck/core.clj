;;##########################################
;;# 2017/9/15 日付比較関係をclj-timeを使って修正
;;##########################################
;;(ns wbsautocheck.core)
(ns wbsautocheck.core
  (:require [clojure.data.json :as json]) ; 2017.8.9
  (:gen-class)
  (:require [clj-time.core :as t])
  (:import [java.util.SimpleTimeZone])
  (:import [java.util.Calendar]))

(use 'dk.ative.docjure.spreadsheet) ; docjure

;;(require 'wbsautocheck.chrono) ; chrono 2017.9.14 だめ。コンパイルエラーいろいろ出る。自前でやるか。。



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
    ;;    (def today (new java.util.Date))
    (def today (t/minus (t/today) (t/days 1))) ;今日 - 1日を、今日とする。
    (def v1 []) ; カラvectorでないとだめ。カラmap{}だと足されていかない。 
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
      (if (not= (:kou1 r1) nil) (do (def ckou1 (:kou1 r1)) (def ckou2 0) (def ckou3 0) (def ckou4 0) ))
      (if (not= (:kou2 r1) nil) (do (def ckou2 (:kou2 r1))               (def ckou3 0) (def ckou4 0) ))
      (if (not= (:kou3 r1) nil) (do (def ckou3 (:kou3 r1))                             (def ckou4 0) ))
      (if (not= (:kou4 r1) nil) (def ckou4 (:kou4 r1)))
      (if (and (not= (:yo-st r1) nil) (not= (:yo-en r1) nil)
               (= (.toString (type (:yo-st r1))) "class java.util.Date")
               (= (.toString (type (:yo-en r1))) "class java.util.Date"))
        (do
;;        (printf "%d.%d.%d.%d: %s 開始(予定)=%s 完了(予定)=%s 担当=%s 状況=%s\n"
;;                (int ckou1) (int ckou2) (int ckou3) (int ckou4)
;;                (str (:taskname1 r1) (:taskname2 r1) (:taskname3 r1) (:taskname4 r1))
;;                (.format (java.text.SimpleDateFormat. "yyyy/MM/dd") (:yo-st r1))
;;                (.format (java.text.SimpleDateFormat. "yyyy/MM/dd") (:yo-en r1))
;;                (:tantou r1)
;;                (if (and (. (:yo-en r1) before today) (= (:ji-en r1) nil)) "完了遅れ"
;;                    (if (and (. (:yo-st r1) before today) (= (:ji-st r1) nil)) "開始遅れ" ""))
;;                );;end-of-printf
;;         (format "%d.%d.%d.%d" (int ckou1) (int ckou2) (int ckou3) (int ckou4))
        (def v1 (conj v1 (array-map :項番   (str (int ckou1) "." (int ckou2) "." (int ckou3) "." (int ckou4)),
         :タスク名 (str (:taskname1 r1) (:taskname2 r1) (:taskname3 r1) (:taskname4 r1)),
         :開始予定 (.format (java.text.SimpleDateFormat. "yyyy/MM/dd") (:yo-st r1)),
         :終了予定 (.format (java.text.SimpleDateFormat. "yyyy/MM/dd") (:yo-en r1)),
;         :開始実績 (if (not= (:ji-st r1) nil) (.format (java.text.SimpleDateFormat. "yyyy/MM/dd") (:ji-st r1)) ""),
;         :終了実績 (if (not= (:ji-en r1) nil) (.format (java.text.SimpleDateFormat. "yyyy/MM/dd") (:ji-en r1)) ""),
         :担当   (:tantou r1),
;;         :status   (if (and (. (:yo-en r1) before today) (= (:ji-en r1) nil)) "完了遅れ"
;;                    (if (and (. (:yo-st r1) before today) (= (:ji-st r1) nil)) "開始遅れ" "")))))
;;         :status   (if (and (. (:yo-en r1) before today) (= (:ji-en r1) nil)) "完了遅れ"
;;                    (if (and (. (:yo-st r1) before today) (= (:ji-st r1) nil)) "開始遅れ" ""))  )))
         :status   (if (and (. (:yo-en r1) before (. today toDate)) (= (:ji-en r1) nil)) "完了遅れ"
                    (if (and (. (:yo-st r1) before (. today toDate)) (= (:ji-st r1) nil)) "開始遅れ" ""))  )))

        );;end-of-出力対応do文
        );;end-of-出力対象if文
      );;end-of-do
    );;end-of-doseq
  ;;(println v1)
  (println (json/write-str v1 :escape-unicode false))
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
    ;;(println "WBS Excelファイルのフルパス:" (first args))
    ;;(println "WBS Excelのシート名:" (second args))
    (if (and (.exists (clojure.java.io/as-file (first args)))
             (not= (second args) nil))
      ;;引数指定したファイル存在する。ここから処理本体
      (do
        ;(println "指定したExcelファイル存在します。")
        (wbsautocheck.core/wbs2json (first args) (second args))
        );;end-of-trueDo        
      ;;引数が不正
      (println "引数指定したファイルが存在しません。")
      )))
