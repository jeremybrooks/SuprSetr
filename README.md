# SuprSetr
## advanced set management for Flickr


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

# 3.0.0 beta release
This is a beta release of SuprSetr that will work with the new https Flickr API endpoints.
It probably has bugs. But don't worry, I'll fix them when they are found.

## Known Issues
 * Sets filtered by video display incorrect counts after a refresh, but are correct when SS restarts.
 
## Testing
Things that have been tested and seem to work. Parenthesis indicate the operating
system that it has been tested on (Mac, Linux, Windows)

 * Creating new sets based on tags (M)
 * Creating new sets with tags and negative tags (M)
 * Sets with limit on photo count (M)
 * Creating sets by date taken (M)x
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

Things that still need some testing and may not work

 * Creating sets based on machine tags
 * Lots of combinations of set criteria
 * FavrTagr
 * Proxy settings
 * Tweet when updated


# ROADMAP
 * 3.0.0

	use new Jinx library so API endpoints will all be https


 * 3.1.0

 	Support multiple accounts
	Full text searches


# DEVELOPERS

This was developed with IntelliJ and the JFormDesigner plugin - http://www.formdev.com
If you are going to make changes to the forms, please use the JFormDesigner form
builder tool to do it so that the source code it generates will not get messed
up. Thanks!

Dependencies are handled by Maven. Any reasonably up-to-date IDE should be able
to import the project and manage the dependencies for you.

