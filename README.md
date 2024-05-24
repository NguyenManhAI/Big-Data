### Naive Bayes Classifer
## Giới thiệu:
- Các tài liệu liên quan được đặt trong thư mục NaiveBayesClassifer, bao gồm:
    + Source code (tệp src):
        * Trong src, class chính là NaiveBayes.class.
    + File jar được build sẵn (NaiveBayesClassifer.jar) [Link](https://drive.google.com/file/d/10IrveJ5hZj5Um3idVdusQRmj55RCihDA/view?usp=sharing).
    + 2 tệp ExampleTrain.txt, ExampleTest.txt để chạy ví dụ.
## Chạy:
- Có thể sử dụng file jar có sẵn hoặc tự build.
- Chạy lệnh: hadoop jar `<file.jar>` `<Train.txt>` `<Test.txt>` `<List of classes>` `<ouput>`
- Vd: hadoop jar NaiveBayesClassifer.jar ExampleTrain.txt ExampleTest.txt "1,2" output
- Kết quả có dạng: Text `<tab>` class1:probability1,class2:probability2,...
- Lưu ý về file Train, Test:
    + file Train: mỗi dòng là 1 cặp Text, class, cách nhau bởi dấu tab
    + file Test: mỗi dòng là Text.
## Các phiên bản kèm theo:
- Java 11 (openjdk 11.0.22 2024-01-16)
- Hadoop 3.4.0
- CoreNLP 4.5.7 [Link](https://stanfordnlp.github.io/CoreNLP/) (Khi buid thêm các thư viện này vào trong dự án).
