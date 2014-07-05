package de.trivago.missionmoon.compass;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class SuccessDialogFragment extends DialogFragment {
	private final static String TAG = "SuccessDialogFragment";
	Place place;

	EditText nameText;
	Button addToGuestbookButton;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		View dialogView = LayoutInflater.from(getActivity()).inflate(
				R.layout.dialog_success, null);

		TextView title = (TextView) dialogView
				.findViewById(R.id.textViewDialogSuccessTitle);
		title.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),
				"fonts/cola.ttf"));

		ListView listView = (ListView) dialogView
				.findViewById(R.id.listViewDialog);
		listView.setAdapter(new GuestbookAdapter());

		nameText = (EditText) dialogView.findViewById(R.id.editTextName);
		addToGuestbookButton = (Button) dialogView
				.findViewById(R.id.buttonAddToGuestbook);

		TextView listViewTitle = (TextView) dialogView
				.findViewById(R.id.textViewGuestbookTitle);
		if (place.getGuestbookEntries().size() == 0)
			listViewTitle.setVisibility(View.INVISIBLE);

		addToGuestbookButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (nameText.getText() != null
						&& nameText.getText().length() > 0)
					dispatchTakePictureIntent();
			}
		});

		return new AlertDialog.Builder(getActivity())
				.setView(dialogView)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// ((FragmentAlertDialog)getActivity()).doPositiveClick();
							}
						}).create();
	}

	private static final int PHOTO_CODE = 132;

	private void dispatchTakePictureIntent() {
		if (isIntentAvailable(MediaStore.ACTION_IMAGE_CAPTURE)) {
			Intent takePictureIntent = new Intent(
					MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(takePictureIntent, PHOTO_CODE);
		} else {
			Toast.makeText(getActivity(), "No Photo App installed",
					Toast.LENGTH_SHORT).show();
			Log.d(TAG, "No photo app installed");
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PHOTO_CODE) {
			Bundle extras = data.getExtras();
			if (extras != null) {

				Bitmap bmp = (Bitmap) extras.get("data");
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream);
				byte[] imageData = stream.toByteArray();

				WebService.sendGuestbookEntry(getActivity(), place, nameText
						.getText().toString(), "", imageData);

				dismiss();
			}
		}
	}

	public boolean isIntentAvailable(String action) {
		final PackageManager packageManager = getActivity().getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	class GuestbookAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return place.getGuestbookEntries().size();
		}

		@Override
		public Object getItem(int position) {
			return place.getGuestbookEntries().get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			if (rowView == null)
				rowView = getActivity().getLayoutInflater().inflate(
						R.layout.row_guestbook, null);

			TextView nameText = (TextView) rowView
					.findViewById(R.id.textViewRowDialogName);
			TextView dateText = (TextView) rowView
					.findViewById(R.id.textViewRowDistance);
			final ImageView image = (ImageView) rowView
					.findViewById(R.id.imageViewDialogImage);

			GuestbookEntry entry = place.getGuestbookEntries().get(position);
			Date date = new Date(entry.created * 1000);

			nameText.setText(entry.user);
			dateText.setText(date.getDay() + "." + date.getMonth() + "."
					+ date.getYear());

			String url = entry.imageUrl;
			if (url != null && url.length() > 0) {
				WebService.client.get(url, new BinaryHttpResponseHandler() {
					@Override
					public void onSuccess(byte[] data) {
						Bitmap x = BitmapFactory.decodeByteArray(data, 0,
								data.length);
						if (x != null)
							image.setImageBitmap(x);
					}
				});
			}

			return rowView;
		}
	}

	// private List<GuestbookEntry> getTestData(){
	// ArrayList<GuestbookEntry> entries = new ArrayList<GuestbookEntry>();
	// GuestbookEntry entry = new GuestbookEntry();
	// entry.created = 829302180;
	// entry.user = "Max Tester";
	//
	// GuestbookEntry entry2 = new GuestbookEntry();
	// entry2.created = 328193211;
	// entry2.user = "Sabine Neumann";
	//
	// entries.add(entry2);
	// entries.add(entry);
	// return entries;
	// }
}
