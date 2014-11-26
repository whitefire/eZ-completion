Provides code-completion for the eZ-API
---------------------------------------

Installation:
-------------
1. Install PhpStorm plugin.
2. Install this bundle: https://github.com/whitefire/ez-completion-bundle

Usage:
------
Provides completion for the ContentTypeService

Todo:
-----
* Validate location of console-executable (consoleProvider)
* Support for remote consoleProviders using either deployment settings or selected server.
* Button for refreshing cache-completion.
* Clear-cache button with support for remote servers.
* Assetic-watch toggle with support for remote servers.
* Auto-poup completions if possible.
* Add twig-completions for content/location
* Implment psi-pattern-matcher for completion-contributor.
* Add completions for search-critera.
* Use PSR-4 for bundle-autoloading.
* Extract method from extend in eZCompletionContributor-constructor.

Known bugs:
----------
Haven't found the correct way of grabbing the currently active project yet.
Consequence: One project at a time only.

On first completion: Language with ID 'PHP' is already registered: class com.jetbrains.php.lang.PhpLanguage