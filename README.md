# 📚 PDF Manager

> Um gerenciador de arquivos PDF para organização de materiais acadêmicos.

---

## Descrição

O **PDF Manager** é uma aplicação Java que permite ao usuário organizar arquivos PDF locais em uma **biblioteca estruturada**. Os arquivos são organizados automaticamente em pastas, separados pelo **primeiro autor**, além de serem registrados em uma base de dados no formato **JSON**.

Atualmente, o sistema suporta três tipos de documentos:

- 📝 **ClassNotes**
- 📖 **Books**
- 📑 **Slides**

---

## Funcionalidades

- ✅ Adicionar arquivos locais à biblioteca
- ✅ Classificar arquivos por tipo (**Books, ClassNotes, Slides**)
- ✅ Organizar os arquivos em pastas nomeadas pelo **primeiro autor**
- ✅ Manter um banco de dados em JSON com informações dos arquivos
- ✅ Listar os arquivos de forma centralizada
- ✅ Editar informações cadastradas (como título, autor ou caminho)
- ✅ Remover arquivos da biblioteca e do banco de dados

---

## Organização da Biblioteca

Quando você cadastra um arquivo, ele é:

1. **Copiado** da sua localização original para a pasta da biblioteca.
2. Organizado na seguinte estrutura:<br>
```bash
/library1
  └── Autor1/
     └── arquivo1.pdf
     └── arquivo2.pdf
  └── Autor2/
     └── arquivo3.pdf
```

4. Registrado no arquivo `books.json`, `classnotes.json` ou `slides.json`, dependendo do tipo de arquivo, contendo informações como:
```json
[ {
  "title" : "example.pdf",
  "authors" : [ "Filipe Campos", "Glênio Queiroz" ],
  "path" : "/home/user/Documents",
  "subTitle" : "Database example",
  "fieldOfKnowledge" : "Computer Science",
  "publishYear" : 2025
} ]
```

---

## Tecnologias Utilizadas
- Java 21
- Maven (Gerenciador de dependências)
- Jackson (Manipulação de JSON)
- Rest-Assured (Parsing de JSON via JsonPath no backend)

