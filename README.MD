
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