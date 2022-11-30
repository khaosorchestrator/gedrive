# Gedrive
## Spring Boot, Google Cloud, Spring Web, OAUTH, Rest API
## Steps to Setup

**1. Clone this repository**
```bash
git clone https://github.com/Lucas24D/gedrive.git
```
**2. Authorize credentials for a Web Application in your Google Cloud**

To authenticate as an end user and access user data in your app, you need to create one or more OAuth 2.0 Client IDs. A client ID is used to identify a single app to Google's OAuth servers.

In the Google Cloud console, go to Menu `===` APIs & Services `>` **Credentials**.

Click Here `->` [Access Credentials Menu](https://console.cloud.google.com/apis/credentials)

1. Click **Create Credentials** `->` **OAuth client ID**.
2. Click **Application type** `->` **Web Application**.
3. In the **Name** field, type a name for the credential. This name is only shown in the Google Cloud console.
4. In **Authorized JavaScript origins** click on **ADD URI** and add the HTTP origins that host your web application. For example (http://localhost:2424)
5. In **Authorized redirect URIs** click on **ADD URI** and add the redirect path. For example (http://localhost:2424/Callback)
6. Click **Create**. The OAuth client created screen appears, showing your new Client ID and Client secret.
7. Click **OK**. The newly created credential appears under **OAuth 2.0 Client IDs**.
8. Save the downloaded JSON file as credentials.json, and move the file to **resources** folder in your project.
9. **Run the app using your IDE.**

**The origins and redirect path port, must not be the same as your local server port!!!**

The app will start running at <http://localhost:8080>

### Explore REST APIs

#### Files API
| Method | Url                               | Description                                    | Sample Valid Request Body |
|--------|-----------------------------------|------------------------------------------------|---------------------------|
| GET    | /api/v1/files                     | Get all files in your google drive root folder | [JSON](#findAll)          |
| GET    | /api/v1/files/{folderId}          | Get all files in a folder                      | [JSON](#findAllInFolder)  |
| GET    | /api/v1/files/download            | Download file from your drive                  |                           |
| GET    | /api/v1/files/{fileId}/folderName | Copy file to a specific folder                 |                           |
| DELETE | /api/v1/files/delete/{fileId}     | Delete file from your drive                    |                           |
| POST   | /api/v1/files/upload              | Upload file to your drive                      |                           |

#### Folders API
| Method | Url                               | Description                                      | Sample Valid Request Body  |
|--------|-----------------------------------|--------------------------------------------------|----------------------------|
| GET    | /api/v1/folders                   | Get all folders in your google drive root folder | [JSON](#findAllFolders)    |
| GET    | /api/v1/folders/{folderName}      | Get folder ID by folder name                     |                            |
| GET    | /api/v1/folders/download          | Download folder from your drive as zip file      |                            |
| POST   | /api/v1/folders                   | Create folder in your drive root folder          |                            |
| DELETE | /api/v1/folders/delete/{folderId} | Delete folder from your drive                    |                            |


##### <a id="findAll">List All Files `->` /api/files</a>
```json
[
  {
    "id": "uyWnpnRGxPYnZ3S1k",
    "name": "Super Manual de comandos do GNU Linux",
    "link": "https://drive.google.com/file/d/uyWnpnRGxPYnZ3S1k/view?usp=sharing",
    "size": "311843",
    "thumbnailLink": null,
    "shared": true
  },
  {
    "id": "xRehO2H4ZNfqW",
    "name": "Lucas Almeida - CV.pdf",
    "link": "https://drive.google.com/file/d/xRehO2H4ZNfqW/view?usp=sharing",
    "size": "74056",
    "thumbnailLink": "https://lh4.googleusercontent.com/6kJvZA1sqz4zyBgy-cKx4epIwh_c8iGDnWGbTIvMzq0xMeW-oJLjUyLE=s220",
    "shared": true
  },
  {
    "id": "3ES5vZVTNqJr53MnHnL8_E",
    "name": "Lucinda_Teixeira_CV",
    "link": "https://drive.google.com/file/d/3ES5vZVTNqJr53MnHnL8_E/view?usp=sharing",
    "size": "8436",
    "thumbnailLink": "https://docs.google.com/feeds/vt?gd=true&id=3ES5vZVTNqJr53MnHnL8_E=14&s=AMedNnoAA46-ZdfsLfQ",
    "shared": true
  },
  {
    "id": "14HDJDAHK9",
    "name": "Investe.me Pitch.pptx",
    "link": "https://drive.google.com/file/d/14HDJDAHK9/view?usp=sharing",
    "size": "88787535",
    "thumbnailLink": "https://lh6.googleusercontent.com/XhuhZgZoyq22HS1DJa832322V0wMCXqt0Kb_tUoE3i0k2meuY2GpgyMIVGb0A=s220",
    "shared": true
  }
]
```

##### <a id="findAllInFolder">List All files in  a folder `->` /api/files/{folderId}</a>
```json
[
  {
    "id": "9rUoYOVnnGb-aHDV3iuxN",
    "name": "Lucas_Almeida_-_ (4).docx",
    "link": "https://drive.google.com/file/d/9rUoYOVnnGb-aHDV/view?usp=sharing",
    "size": "20921",
    "thumbnailLink": null,
    "shared": false
  },
  {
    "id": "1eS10kr-zyH2ul3G",
    "name": "Lucas_Almeida_-_ (4) (copy).docx",
    "link": "https://drive.google.com/file/d/1eS10kr2ul3G/view?usp=sharing",
    "size": "21330",
    "thumbnailLink": null,
    "shared": false
  }
]
```

##### <a id="findAllFolders">List All folders  `->` /api/folders</a>
```json
[
  {
    "id": "1AGwRCpCrAqL6sQ46qocV-oEV3SvU7AuN",
    "name": "Meus Documentos",
    "link": "https://drive.google.com/drive/u/0/folders/1AGwRCpCrAqL6sQ46qocV-oEV3SvU7AuN"
  },
  {
    "id": "1DDr1oUu_0ujNPzvf0mgtiNJNTRnpmvi6",
    "name": "MyCV",
    "link": "https://drive.google.com/drive/u/0/folders/1DDr1oUu_0ujNPzvf0mgtiNJNTRnpmvi6"
  },
  {
    "id": "1AWDmaDTtVF0AyZO81yk",
    "name": "Ataque de Repúdio",
    "link": "https://drive.google.com/drive/u/0/folders/1AWDmaDTtVF0AyZO81yk"
  }
]
```
