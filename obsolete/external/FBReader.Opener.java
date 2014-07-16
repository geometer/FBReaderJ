	private class ExtFileOpener implements FBReaderApp.ExternalFileOpener {
		private void showErrorDialog(final String errName) {
			runOnUiThread(new Runnable() {
				public void run() {
					final String title = ZLResource.resource("errorMessage").getResource(errName).getValue();
					final AlertDialog dialog = new AlertDialog.Builder(FBReader.this)
						.setTitle(title)
						.setIcon(0)
						.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
							}
						})
						.create();
					if (myIsPaused) {
						myDialogToShow = dialog;
					} else {
						dialog.show();
					}
				}
			});
		}

		public boolean openFile(ZLFile f, String appData) {
			if (f == null) {
				showErrorDialog("unzipFailed");
				return false;
			}
			String extension = f.getExtension();
			Uri uri = Uri.parse("file://" + f.getPath());
			Intent launchIntent = new Intent(Intent.ACTION_VIEW);
			launchIntent.setPackage(appData);
			launchIntent.setData(uri);
			FileType ft = FileTypeCollection.Instance.typeForFile(f);
			for (MimeType type : ft.mimeTypes()) {
				launchIntent.setDataAndType(uri, type.Name);
				try {
					startActivity(launchIntent);
					return true;
				} catch (ActivityNotFoundException e) {
				}
			}
			showErrorDialog("externalNotFound");
			return false;
		}
	}

