# Contributing

Thanks for your interest in contributing! We want you working on things you're excited about.

To make things easier for everyone, please read this guide in detail.

## Developer resources:

Here are some important resources:

 * Issue and bug tracking: [Github Issues](https://github.com/AlejandroRivera/embedded-rabbitmq/issues)
 * Want to chat? Come find us in [Gitter](https://gitter.im/io-arivera-oss/embedded-rabbitmq). 
 * Documentation is kept inside this project's [Github Wiki](https://github.com/AlejandroRivera/embedded-rabbitmq/wiki)

## Testing

Please write Unit Tests for new code you create. We are currently using JUnit. 

## Submitting changes

Please send a GitHub Pull Request with a clear list of what you've done and why.
When you send a pull request, we will love you forever if you include Unit Tests. 
We can always use more test coverage as well! 

Our coding convention is automatically enforced by the build itself.
A few other quality checks are also automated. If the build fails, please fix it since broken builds won't be reviewed. 

Always write a clear log message for your commits. One-line messages are fine for small changes, but bigger changes should look like this:

> A brief summary of the commit
> 
> A paragraph describing what changed and its impact.

## Reporting bugs

Please write a detailed bug report. This typically consists of:

  * Title: a concise summary of the issue
  * Steps to reproduce
  * Actual behavior
  * Expected behavior
  * Environment (Operating System + Version, Java Version, etc.)
  * Optionally: 
    - Known workarounds
    - Logs + Stacktraces (if too big, include a link to a public Gist)

We don't expect everyone to do so, but it would be awesome if the bug report included a Unit Tests that only breaks under the 
given conditions and passes when the bug is resolved.

## Writing documentation

Feel free to enhance the existing documentation in any way you see fit. Just keep it friendly yet professional, following proper
grammar and vocabulary. The official language of this project is English, but translations are always welcomed.
 
## Developer environment

### Requirements:

You will need the following tools installed on your computer: 
  * Maven 3.X
  * Java SDK 7+
  * Git
 
### Quick start:
  1. Fork this project on Github to make a copy of it under your own account.
  1. Clone your forked project to get a copy on your development machine and branch out
    ```
    $ git clone ssh://github.com/{yourUsername}/embedded-rabbitmq
    $ git branch -b {branch_name}
    ```
    Try to stick to the convention of branch names `feature/{name}` for new functionality, and
    `fix/{name}` 
  1. Load the project into your favorite IDE or text editor
  1. Make changes to the source
  1. Commit your changes locally and push them to Github
    ```
    $ git add .
    $ git commit -m "Descriptive commit message"
    $ git push origin {branch_name}
    ```
  1. Create new Pull Request on Github.
