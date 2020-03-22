# SuprSetr
## advanced album management for Flickr

When you start SuprSetr, it will connect to Flickr and get a list of all your
sets. Data about these sets will be stored in a local database, and updated
every time you launch SuprSetr. This process takes a little while the first
time SuprSetr is launched. Subsequent launches will be faster.

The first time you launch SuprSetr, none of the sets are managed. You can add a
new set by selecting Edit -> Create Set from the menu, or edit an existing set
by double clicking it in the list or selecting Edit -> Edit Set from the menu.
After saving the changes, the set will be refreshed on Flickr, and the list will
 be updated as well.

Sets can also be deleted by selecting Edit -> Delete Set from the menu. Deleting
a set will remove it permanently from Flickr, but will not delete the photos
that it contains.

To refresh a set, select Edit -> Refresh Set from the menu. Currently, a set
can be refreshed at any time. Sets are displayed in the list sorted by
managed/unmanaged, then alphabetically. Managed sets will always display at the
top of the list. Additional sort options may be added in the future if users
want that feature.

## Installers
If you are just looking for the installer, head over to [the SuprSetr home page](http://jeremybrooks.net/suprsetr)

## Known Issues

 * Flickr seems to limit sets to 4,500 images
 * Picking a set image doesn't work when using proxy
 * If you don't limit searches using tags or dates, Flickr ignores other options

# ROADMAP
SuprSetr is pretty much feature complete. There are some minor improvements that might be nice, but there are no firm plans to support these features:

 * Split large search results into multiple sets, effectively bypassing the 4500 photo limit
 * Support multiple accounts
 * Other social media integration

# DEVELOPERS

This was developed with IntelliJ and the JFormDesigner plugin - http://www.formdev.com
If you are going to make changes to the forms, please use the JFormDesigner form
builder tool to do it so that the source code it generates will not get messed
up. Thanks!

Dependencies are handled by Maven. Any reasonably up-to-date IDE should be able
to import the project and manage the dependencies for you.

