# GIT-Find-The-Skills
The GOAL of GIT-Find-The-Skills is the classification of the commiters present in a git repository. Each user may have collaboratored differently in a project, in particular he can be a backend, frontend developer, or writer. 

## Setup
Edit the file [config.properties](https://github.com/Tkd-Alex/GIT-Find-The-Skills/blob/master/gittocv/config.properties) as you need.
- **repository** https address of the repo or global path in your PC.
- **backend** list of extensions to classify as backend.
- **frontend** list of extensions to classify as frontend.
- **writer** list of extensions to classify as writer.
- **undefined** list of exstension without a exactly category. The script already manager in different way exstension like: 
   - .js
   - .java
   - .php
- **java_fe** list of Java packages to classify as frontend.
- **export_as**
  - If you choose **HTML** the output will be a .zip with a _index.html_ and other .css, .js file useful for the disaply. 
  - If you choose **csv** the ouputl will be a .csv file.
- **...** extra category can be defined in this file with:
```
category_name=pkg1;pkg2;pkg3
```
Use the char **;** as separator for each library/extensions.

## Output
- HTML is a web page that contanis a card for each user and the relative data:
  - Name
  - Email
  - Number of commits (analyzed)
  - Percentage for each category
  - Extra 'social' info

![HTMLExample](https://github.com/Tkd-Alex/Report-Latex/blob/master/git_find_the_skills/img/htmloutput.png)
- csv output is a table with the following value:


| Column         | Description                                    |
|----------------|------------------------------------------------|
| Name           | Name and surname of commiter                   |
| Email          | Email                                          |
| SocialID       | ID founded on the hosting/git service          |
| SocialUsername | Username used on the hosting/git service       |
| AvatarURL      | Avatar used on the hosting/git service         |
| WebSite        | ---                                            |
| Location       | ---                                            |
| Bio            | Biography                                      |
| CreatedAt      | Creation date of the account on the git sevice |
| Commits        | Number of commits (analyzed)                   |
| Backend%       | Percentage of backend category                 |
| Frontend%      | Percentage of frontend category                |
| Writer%        | Percentage of writer category                  |
| CatExtra...%   | Percentage of extra... category                |

## How it's work

![FlowChart](https://github.com/Tkd-Alex/Report-Latex/blob/master/git_find_the_skills/img/flowchart.png)

_________

Read the full documentation at: [GIT-Find-The-Skills](https://github.com/Tkd-Alex/Report-Latex/blob/master/git_find_the_skills/relazione.pdf)
