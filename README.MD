# Ai Desk

Automate common tasks with the help of AI

- [X] Name scanned PDFs
- [X] Add meta data to scanned PDFs
- [ ] Convert images to PDFs

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