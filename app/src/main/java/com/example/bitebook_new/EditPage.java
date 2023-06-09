package com.example.bitebook_new;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.AutoCompleteTextView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bitebook_new.adapter.PlaceAutoSuggestAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class EditPage extends Fragment {

    ArrayList<Entry> entries;
    EditText menuName, price, foodMemo;
    TextView cancel, addPictures;
    RatingBar rate;
    ImageView pictures;
    Button update;
    Spinner areaSpinner, cuisineSpinner;
    String area, cuisine;
    String image_url = null;
    Bitmap bitmap;
    private Context content;
    private Entry data;
    private int areaIdx;
    int cuisineIdx;
    LatLng latLng;

    TextView responseView;
    PlacesClient placesClient;
    PlaceAutoSuggestAdapter adapter;
    AutoCompleteTextView restaurantName;


    public EditPage(Context content, Entry data) {
        this.content = content;
        this.data = data;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_page, container, false);

        // initialize each key for further inputs from users
        entries = new ArrayList<>();

        // elements' ids with the elements in fragment
        restaurantName = view.findViewById(R.id.restaurantName);
        menuName = view.findViewById(R.id.menuName);
        price = view.findViewById(R.id.price);
        rate = view.findViewById(R.id.ratingBar);
        pictures = view.findViewById(R.id.imageAdd);
        addPictures = view.findViewById(R.id.addPicture);
        foodMemo = view.findViewById(R.id.foodMemo);
        update = view.findViewById(R.id.updateButton);
        areaSpinner = view.findViewById(R.id.areaSpinner);
        cuisineSpinner = view.findViewById(R.id.cuisineSpinner);
        cancel = view.findViewById(R.id.cancel);

        // show the inputs exist already
        restaurantName.setText(data.getResName());
        menuName.setText(data.getMenName());
        price.setText(Integer.toString((int) data.getPrice()));
        rate.setRating(data.getRating());
        foodMemo.setText(data.getReview());


        // set image depends on the current input
        if (data.getImage() != null){
            image_url = data.getImage();
            pictures.getLayoutParams().height = 600;
            addPictures.getLayoutParams().height = 0;
            Picasso.get().load(data.getImage()).into(pictures);
        }

        String apiKey = BuildConfig.KEY;
        System.out.println(apiKey);
        if (apiKey.isEmpty()) {
            responseView.setText("Error");
            return null;
        }

        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initialize(getContext(), apiKey);
        }

        placesClient = Places.createClient(getContext());
        initPlaceAutoSuggestAdapter();

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(
                getContext(), R.array.areaSpinner, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaSpinner.setAdapter(adapter1);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                getContext(), R.array.cuisineSpinner, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cuisineSpinner.setAdapter(adapter2);

        addPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (data.getImage() == null){
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);

                    // set the height of the picture
                    pictures.getLayoutParams().height = 600;
                    addPictures.getLayoutParams().height = 0;

                    startActivityForResult(intent, 1);
                }
            }
        });

        // get the index of the area to set the area in the spinner
        String[] areaList = getResources().getStringArray(R.array.areaSpinner);
        for (int i =0; i < areaList.length; i ++ ){
            if (areaList[i].equals(data.getArea())){
                 areaIdx = i;
                 break;
            }
        }

        // get the index of the cuisine to set the cuisine in the spinner
        String[] cuisineList = getResources().getStringArray(R.array.cuisineSpinner);
        for (int i =0; i < cuisineList.length; i ++ ){
            if (cuisineList[i].equals(data.getCuisine())){
                cuisineIdx = i;
                break;
            }
        }

        areaSpinner.setSelection(areaIdx);
        cuisineSpinner.setSelection(cuisineIdx);

        areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                area = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                area = null;
            }
        });

        cuisineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                cuisine = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                cuisine = null;
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get String input from the element
                String resName = restaurantName.getText().toString();
                String menName = menuName.getText().toString();
                float pri = Float.parseFloat(price.getText().toString());
                float rat = rate.getRating();
                String fooMemo = foodMemo.getText().toString();
                String id = data.getId();
                latLng = data.getLatlng();

                // check any of necessary inputs are empty/ missing
                if (resName.isEmpty() ||
                        menName.isEmpty() ||
                        price.getText().toString().isEmpty() ||
                        rat == 0.0 ||
                        area == null ||
                        cuisine == null) {
                    // if the resName is empty then show a message
                    Toast.makeText(getActivity(), "Please fill in the blanks", Toast.LENGTH_LONG).show();
                } else {
                    // if food memo is empty then save null instead
                    if (fooMemo.isEmpty()) {
                        fooMemo = null;
                    }

                    // change the data in Entry
                    Entry entry = new Entry(id, resName, menName, pri, area, rat, fooMemo, cuisine, image_url, latLng);
                    FirebaseHelper.updateEntry(content, entry, bitmap);
                    Toast.makeText(getActivity(), "YUMMY ! Successfully updated your food", Toast.LENGTH_LONG).show();

                    // go back to homePage
                    HomePage homePage = new HomePage();
                    FragmentManager fragmentManager = ((AppCompatActivity) content).getSupportFragmentManager();

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    fragmentTransaction.replace(R.id.drawer_layout, homePage).commit();

                }

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go back to homePage
                HomePage homePage = new HomePage();
                FragmentManager fragmentManager = ((AppCompatActivity) content).getSupportFragmentManager();

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                fragmentTransaction.replace(R.id.drawer_layout, homePage).commit();
            }
        });

        return view;
    }

    //this method is called when the user inputs text into the AutoComplete Text field
    private void initPlaceAutoSuggestAdapter(){
        restaurantName.setThreshold(1);
        restaurantName.setOnItemClickListener(autocompleteClickListener);
        adapter = new PlaceAutoSuggestAdapter(getContext(), placesClient);
        restaurantName.setAdapter(adapter);
    }

    //this method is the on click listener for the autofill text field
    private final AdapterView.OnItemClickListener autocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            try {
                final AutocompletePrediction item = adapter.getItem(i);
                String placeID = null;
                if (item != null) {
                    placeID = item.getPlaceId();
                }

//                To specify which data types to return, pass an array of Place.Fields in your FetchPlaceRequest
//                Use only those fields which are required.

                List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS
                        , Place.Field.LAT_LNG);

                FetchPlaceRequest request = null;
                if (placeID != null) {
                    request = FetchPlaceRequest.builder(placeID, placeFields)
                            .build();
                }

                if (request != null) {
                    placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onSuccess(FetchPlaceResponse task) {
//                            responseView.setText(task.getPlace().getName() + "\n" + task.getPlace().getAddress());
                            latLng = task.getPlace().getLatLng();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                            responseView.setText(e.getMessage());
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            // Get the Uri of the selected image
            Uri selectedImage = data.getData();

            try {
                image_url = FirebaseHelper.generateRandomString();
                // Use the ContentResolver to get a Bitmap from the Uri
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                // Set the Bitmap to the ImageView
                pictures.setImageBitmap(bitmap);
                // Save the Bitmap to the MyObject instance
//                Entry.setFoodImage(bitmap);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] pictureData = baos.toByteArray();

                FirebaseStorage storage = FirebaseStorage.getInstance();
                // Create a storage reference from our app
                StorageReference storageRef = storage.getReference();
                // Create a child reference
                // imagesRef now points to "images"
                StorageReference imagesRef = storageRef.child(image_url);

                // Child references can also take paths
                // spaceRef now points to "images/space.jpg
                // imagesRef still points to "images"
                UploadTask uploadTask = imagesRef.putBytes(pictureData);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                image_url = uri.toString();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                            }
                        });
                    }
                });



            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

