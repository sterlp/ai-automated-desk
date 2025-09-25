# Ai Desk

Automate common tasks with the help of AI

- [X] Name scanned PDFs
- [X] Add meta data to scanned PDFs
- [X] Convert images to PDFs
- [ ] Join Images to one PDF
- [ ] Join PDFs to one PDF

# Required

- Java 21
- https://github.com/ocrmypdf/OCRmyPDF
- https://ollama.com/

# AI Scores - Einfache LIDL Rechnung "M1 16GB"

## TLDR

1. Try **qwen3:4b** or **gemma3:4b**, if you have not many resources
1. If you have plenty of RAM 24GB / VRAM 16 GB try first **granite3.3:8b** next **qwen3:8b**
1. If you have more RAM (>=32GB) you also test **gemma3:12b** if it creates better results

## Summaries
### Summary: kassenzettel_lidl

- qwen3:4b score: **14/21 = 66%**
    - 2018-02-16_Rechnung_0978182607_02_LIDL_KDK_Ä.pdf
- gemma3:4b score: **15/21 = 71%**
    - 2018-02-16_LIDL_Rechnung_0978_18260702.pdf.pdf
- qwen3:8b score: **12/21 = 57%**
    - 2018-02-16_Rechnung_0978 182607_02_LIDL.pdf
- llama3.1:8b score: **9/21 = 42%**
    - 2018-02-16_Kaufen_LIDL_1627.pdf
- granite3.3:8b score: **17/21 = 80%**
    - 2018-02-16_LidlRechnung_0978182607_02_LIDL LOHNT SICH.pdf
- mistral:7b score: **3/21 = 14%**
    - 2018-02-16-Lieferschein-KDK-AE.pdf
- gemma3:12b score: **10/21 = 47%**
    - 2018-02-16_Kundenbeleg_LIDL.pdf

### Summary: Musterrechnung

- qwen3:4b score: **17/23 = 73%**
    - 2013-12-31_Rechnung_Hotel-Gasthof_Stern_207581.pdf
- gemma3:4b score: **10/23 = 43%**
    - 2013-12-01_Hotel-Gasthof_Stern_207581.pdf
- qwen3:8b score: **16/23 = 69%**
    - 2013-12-01 Rechnung 207581 Hotel-Gasthof Stern.pdf
- llama3.1:8b score: **14/23 = 60%**
    - 03122013_Rechnung_Hotel-Gasthof_Stern_207581.pdf
- granite3.3:8b score: **14/23 = 60%**
    - 2013-12-28_Hotel_Gasthof_Stern_Rechnung_207581.pdf
- mistral:7b score: **12/23 = 52%**
    - 2013-12-Rechnung-207581-Hotel-Gasthof-Stern.pdf
- gemma3:12b score: **19/23 = 82%**
    - 2013-12-31_Rechnung_207581_Hotel-Gasthof Stern.pdf

## Benshmarks

### LIDL Rechnung

![LIDL Rechnung](src/test/resources/kassenzettel_lidl.jpg)

**Results differ a bit from run to run**

#### qwen3:4b
```json
{
  "from": "LIDL",
  "to": "KDK Ä",
  "date": "2018-02-16",
  "documentType": "Rechnung",
  "documentNumber": "0978 182607/02",
  "summary": "Rechnung für 6,38 EUR mit UST-ID-NR: DE250063577",
  "title": "Rechnung für 6,38 EUR",
  "fileName": "2018-02-16_Rechnung_0978182607_02_LIDL_KDK_Ä"
}
```

- qwen3:4b score: **14/21 = 66%**
- 2018-02-16_Rechnung_0978182607_02_LIDL_KDK_Ä.pdf

#### gemma3:4b

```json
{
  "from": "Heegermühlerstr. 1\n16225 Eberswalde",
  "to": "null",
  "date": "16,02,18 15:25 Uhr",
  "documentType": "Rechnung",
  "documentNumber": "0978 182607/02",
  "summary": "Invoice for purchase from LIDL with a total amount of 6.38 EUR, including VAT.",
  "title": "LIDL Rechnung 16.02.2018",
  "fileName": "2018-02-16_LIDL_Rechnung_0978_18260702.pdf"
}
```

- gemma3:4b score: **15/21 = 71%**
- 2018-02-16_LIDL_Rechnung_0978_18260702.pdf.pdf

#### qwen3:8b

```json
{"from":"LIDL",
"to":null,
"date":"2018-02-16",
"documentType":"Rechnung",
"documentNumber":"0978 182607/02",
"summary":"This document is a receipt for a purchase of 6,38 EUR, including items like Sagrotan Gel Kamille, Leder Rein.Tücher, and Schoko-Bananen, paid with a credit card.",
"title":"Receipt for Purchase of 6,38 EUR",
"fileName":"2018-02-16_Rechnung_0978 182607/02_LIDL"
}
```

- qwen3:8b score: **12/21 = 57%**
- 2018-02-16_Rechnung_0978 182607_02_LIDL.pdf

#### llama3.1:8b

```json
{
    "from": "LIDL",
    "to": null,
    "date": "null",
    "documentType": "Kaufvertrag",
    "documentNumber": "1627",
    "summary": "Bestellung über 6,38 EUR bei LIDL",
    "title": "Kaufbeleg vom 16.02.18",
    "fileName": "2018-02-16_Kaufen_LIDL_1627"
}
```

- llama3.1:8b score: **9/21 = 42%**
- 2018-02-16_Kaufen_LIDL_1627.pdf


#### granite3.3:8b

```json
{
  "from": "LIDL LOHNT SICH.",
  "to": null,
  "date": "2018-02-16",
  "documentType": "Rechnung",
  "documentNumber": "0978 182607/02",
  "summary": "Lidl Rechnung Nr. 0978 182607/02 vom 16.02.2018 für Einkäufe im Wert von 6,38 EUR inklusive MWST.",
  "title": "Lidl Rechnung",
  "fileName": "2018-02-16_LidlRechnung_0978182607_02_LIDL LOHNT SICH"
}
```

- granite3.3:8b score: **17/21 = 80%**
- 2018-02-16_LidlRechnung_0978182607_02_LIDL LOHNT SICH.pdf

#### mistral:7b

```json
{
  "from": "KDK Ä",
  "to": null,
  "date": "2018-02-16",
  "documentType": "Lieferschein",
  "documentNumber": null,
  "summary": "Delivery of makeup remover, Sagrotan Gel Kamille, leather cleaning supplies, and Schoko-Bananen with total amount of 6.38 EUR",
  "title": "Delivery of makeup remover, etc.",
  "fileName": "2018-02-16-Lieferschein-KDK-AE"
}
```

- mistral:7b score: **3/21 = 14%**
- 2018-02-16-Lieferschein-KDK-AE.pdf

#### deepseek-r1:7b

```json
{}
```

- Failed!

#### gemma3:12b

```json
{
  "from": "LIDL",
  "to": null,
  "date": "2018-02-16",
  "documentType": "Kundenbeleg",
  "documentNumber": null,
  "summary": "Kundenbeleg über eine Kartenzahlung von 6,38 EUR bei LIDL.",
  "title": "LIDL Kundenbeleg",
  "fileName": "2018-02-16_Kundenbeleg_LIDL"
}
```
- gemma3:12b score: 10/21 = 47%
- 2018-02-16_Kundenbeleg_LIDL.pdf

### Musterrechnung

[Musterrechnung](src/test/resources/Musterrechnung_ocr.pdf)

#### qwen3:4b

```json
{
  "from": "Hotel-Gasthof Stern",
  "to": "null",
  "date": "2013-12-31",
  "documentType": "Rechnung",
  "documentNumber": "207581",
  "summary": "Rechnung für 701,68 EUR mit 10,0% Rabatt (882 EUR) und 19,0% Umsatzsteuer (112,03 EUR) vom Hotel-Gasthof Stern für den Zeitraum bis 31.12.2013",
  "title": "Rechnung Hotel-Gasthof Stern für 2013",
  "fileName": "2013-12-31_Rechnung_Hotel-Gasthof_Stern_207581"
}
```

- qwen3:4b score: **17/23 = 73%**
- 2013-12-31_Rechnung_Hotel-Gasthof_Stern_207581.pdf

#### gemma3:4b

```json
{
  "from": "Hotel-Gasthof Stern",
  "to": "null",
  "date": "01.12.2013",
  "documentType": "Rechnung",
  "documentNumber": "207581",
  "summary": "Invoice for hotel stay and related expenses from Hotel-Gasthof Stern.",
  "title": "Hotel-Gasthof Stern - Invoice 207581",
  "fileName": "2013-12-01_Hotel-Gasthof_Stern_207581"
}
```

- gemma3:4b score: **10/23 = 43%**
- 2013-12-01_Hotel-Gasthof_Stern_207581.pdf

#### qwen3:8b

```json
{
    "from":"Hotel-Gasthof Stern",
    "to":null,"date":"2013-12-01",
    "documentType":"Rechnung",
    "documentNumber":"207581",
    "summary":"This document is a Rechnung from Hotel-Gasthof Stern for services provided from 01.12.2013 to 31.12.2013, with a total amount of EUR 701.68 including 19% VAT.","title":"Rechnung Hotel-Gasthof Stern",
    "fileName":"2013-12-01 Rechnung 207581 Hotel-Gasthof Stern"
}
```
- qwen3:8b score: **16/23 = 69%**
- 2013-12-01 Rechnung 207581 Hotel-Gasthof Stern.pdf

#### llama3.1:8b


```json
{
    "from": "Hotel-Gasthof Stern",
    "to": null,
    "date": "null",
    "documentType": "Rechnung",
    "documentNumber": "207581",
    "summary": "Rechnung für Leistungen vom 01.12.2013 bis 31.12.2013",
    "title": "Rechnung vom 01.12.2013",
    "fileName": "03122013_Rechnung_Hotel-Gasthof_Stern_207581"
}
```

- llama3.1:8b score: 14/23 = 60%
- 03122013_Rechnung_Hotel-Gasthof_Stern_207581.pdf

#### granite3.3:8b

```json
{
  "from": "Hotel-Gasthof Stern",
  "to": null,
  "date": "2013-12-28",
  "documentType": "Rechnung",
  "documentNumber": "207581",
  "summary": "This is a hotel invoice for various linens and kitchen equipment, with a total amount of 701.68 EUR, including 19% VAT.",
  "title": "Hotel-Gasthof Stern - Rechnung 207581",
  "fileName": "2013-12-28_Hotel_Gasthof_Stern_Rechnung_207581"
}
```

- granite3.3:8b score: **14/23 = 60%**
- 2013-12-28_Hotel_Gasthof_Stern_Rechnung_207581.pdf

#### mistral:7b


```json
{
  "from": "Hotel-Gasthof Stern",
  "to": null,
  "date": "2013-12-01",
  "documentType": "Rechnung",
  "documentNumber": "207581",
  "summary": "Invoice for services and items provided from December 1 to December 31, 2013",
  "title": "Invoice 2013-12 for Hotel-Gasthof Stern",
  "fileName": "2013-12-Rechnung-207581-Hotel-Gasthof-Stern"
}
```

- mistral:7b score: **12/23 = 52%**
- 2013-12-Rechnung-207581-Hotel-Gasthof-Stern.pdf

#### gemma3:12b

```json
{
    "from": "Hotel-Gasthof Stern, Postfach 20 23, 86310 Pfaffenhausen", 
    "documentType": "Rechnung", 
    "documentNumber": "207581", 
    "date": "2013-12-31", "summary": "Rechnung über 701,68 EUR für Hotel-Gasthof Stern, inklusive Mehrwertsteuer, mit einem Skonto von 14,03 EUR, zur Lastschrift am 23.01.2044.", 
    "title": "Rechnung Hotel-Gasthof Stern", 
    "fileName": "2013-12-31_Rechnung_207581_Hotel-Gasthof Stern"
}
```

- gemma3:12b score: **19/23 = 82%**
- 2013-12-31_Rechnung_207581_Hotel-Gasthof Stern.pdf


# ocrmypdf

`docker run -i --rm jbarlow83/ocrmypdf --skip-text -l deu - - < in.pdf > out.pdf`

```
ProcessBuilder pb = new ProcessBuilder(
        "docker", "run", "-i", "--rm", "jbarlow83/ocrmypdf", "-l", "deu", "-", "-"
);
```

```
for filename in ./*.pdf; do
  docker run — rm -v=$(pwd):/tmp jbarlow83/ocrmypdf -l deu /tmp/${filename#./} /tmp/out/${filename#./}
done
```

# Using / based on

- https://github.com/ocrmypdf/OCRmyPDF
- https://ollama.com/
- https://pdfbox.apache.org/
- https://spring.io/projects/spring-boot