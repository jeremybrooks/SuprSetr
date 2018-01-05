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
 
## Testing
Things that have been tested and seem to work. Parenthesis indicate the operating
system that it has been tested on (Mac, Linux, Windows)

 * Creating new sets based on tags (M)
 * Creating new sets with tags and negative tags (M)
 * Sets with limit on photo count (M)
 * Creating sets by date taken (M)
 * Updating sets by date taken (M)
 * Editing the primary photo for a set (M)
 * Editing title and description for a set (M)
 * Uses your existing legacy SuprSetr auth token and converts to an OAuth token (M)
 * Legacy auth token is deleted (M)
 * New users can authorize using OAuth workflow (M)
 * Uses existing database - no loss of set definitions (M)
 * Open set in browser (M)
 * Deleting sets (M)
 * Activity Log (M)
 * Hide Unmanaged Albums (M)
 * Case sensitive sort (display only) (M)
 * Compress logs (M)
 * Enable/Disable detailed logging (M)
 * View tutorial (M)
 * Help message (M)
 * Authorize Twitter (M)
 * Tweet when created (M)
 * Tweet when updated (M)
 * Proxy settings (M)
 * Creating sets based on machine tags (M)
 
Things that still need some testing and may not work

 * Lots of combinations of set criteria
 * FavrTagr


# ROADMAP
 * 3.2.0

	FavrTagr can use machine tags (fave:count=10 for example) or regular tags
	Better control of when FavrTagr creates tags
	Support experimental search options (color, style, orientation)

 * 3.?.?
    Split large search results into multiple sets
 	Facebook integration
 	
 * ?
 	Support multiple accounts?
 	Other social media integration?

# DEVELOPERS

This was developed with IntelliJ and the JFormDesigner plugin - http://www.formdev.com
If you are going to make changes to the forms, please use the JFormDesigner form
builder tool to do it so that the source code it generates will not get messed
up. Thanks!

Dependencies are handled by Maven. Any reasonably up-to-date IDE should be able
to import the project and manage the dependencies for you.

