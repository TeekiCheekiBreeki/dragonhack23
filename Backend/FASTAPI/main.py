# Import necessary modules
import datetime
import io
import os
from fastapi import FastAPI, File, HTTPException, Header, UploadFile, Query
from pymongo import MongoClient
from bson.objectid import ObjectId
import uuid
from fastapi.responses import StreamingResponse, FileResponse
from pathlib import Path
from fastapi.middleware.cors import CORSMiddleware
from pymongo import GEOSPHERE
from pydantic import BaseModel
from typing import List





# Super secret key, but visible to code reviewers because we're nice for now - simple solution would be to add it to secrets.py and git ignore it.
SECRET_KEY = "WeLoveDragonhack"





# Create a Pydantic model for the location coordinates
class Point(BaseModel):
    type: str = "Point"
    coordinates: List[float]

# Create the FastAPI app instance
app = FastAPI()
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Connect to the MongoDB server
client = MongoClient("mongodb:27017")

# Select the database to use
db = client["sonic_graffiti"]

# Select the collection to use
audios = db["audio_files"]
audios.create_index([("entry_id", 1)], unique=True)
audios.create_index([("location", "2dsphere")])  # Add this line to create a 2dsphere index for the "location" field






# Create an endpoint to handle file uploads
@app.post("/uploadfile/")
async def create_upload_file(title: str,latitude: float = Query(..., description="Latitude of the location"),
    longitude: float = Query(..., description="Longitude of the location"), file: UploadFile = File(...)):
    # Extract the file extension from the filename
    extension = file.filename.split(".")[-1]

    # Generate a unique filename using UUID and the file extension
    filename = str(uuid.uuid4()) + "." + extension

    # Open a file in binary write mode and write the file contents to it
    with open(filename, "wb") as f:
        f.write(await file.read())


    # To get width and height
    with open(filename, "rb") as f:
        img_bytes = io.BytesIO(f.read())
    

    # Insert a new document into the MongoDB collection with the file metadata
    entry_id = audios.count_documents({}) + 1
    audios.insert_one({
        "entry_id": entry_id,
        "title": title,
        "filename": filename,
        "size_MB": os.path.getsize(filename) / (1024 * 1024),
        "num_likes": 0,
        "num_dislikes": 0,
        "uploaded_time": datetime.datetime.now(),
        "location": {"type": "Point", "coordinates": [longitude, latitude]}  # Replace "longitude" and "latitude" with the actual values
    })

    # Return the ID of the new document as a JSON response
    return {"entry_id": entry_id}



# Create an endpoint to handle audio updates
@app.patch("/audios/{entry_id}")
async def update_audio_title(entry_id: str, new_title: str):
    # Update the document in the collection with the given ID
    audios.update_one({"entry_id": int(entry_id)}, {"$set": {"title": new_title, "title_modified": True}})

    # Return a success message as a JSON response
    return {"message": "audio title updated"}

# Create an endpoint to get an audio with a given ID
@app.get("/audios/{entry_id}")
async def get_audio(entry_id: str):
    file_extensions = (".wav", ".mp3", ".m4a")
    
    audio = audios.find_one({"entry_id": int(entry_id)})
    if audio:
        # Check if the audio file exists
        print("audio yes")
        if Path(audio["filename"]).is_file():
            # Check if the file extension is valid
            print("File yes")
            if audio["filename"].endswith(file_extensions):
                # Return the audio as a StreamingResponse
                return StreamingResponse(open(audio["filename"], "rb"), media_type="audio/*")
            else:
                raise HTTPException(status_code=415, detail="Unsupported media type")
        else:
            raise HTTPException(status_code=404, detail="audio file not found")
    
    
    

# Create an endpoint to get all audios
@app.get("/audios/")
async def get_all_audios():
    # Find all documents in the collection
    audio_docs = audios.find()

    # Convert the documents to a list of dictionaries
    audio_list = [doc for doc in audio_docs]
    
    modified_audio_list = []
    for audio in audio_list:
        audio["entry_id"] = str(audio["entry_id"])
        modified_audio_list.append(audio)
    
    # Return the list of audios as a JSON response
    return {"audios": modified_audio_list}


# Create an endpoint to get the title of an audio with a given ID
@app.get("/audios/{entry_id}/title")
async def get_audio_title_byentry_id(entry_id: str):
    audio = audios.find_one({"entry_id": int(entry_id)})
    if audio:
        return {"title": audio["title"]}
    else:
        raise HTTPException(status_code=404, detail="audio not found")
    
# Create an endpoint that tells how many audios are in the database
@app.get("/audios_count/")
async def count_audios():
    count = audios.count_documents({})
    return {"count": count}

# Create an endpoint to get the highest entry_id
@app.get("/highest_entry_id/")
async def get_highest_entry_id():
    highest_id = audios.count_documents({})
    return {"highest_id": highest_id}

# Create an endpoint to get locations within a 20km radius
@app.get("/audios/nearby/")
async def get_audios_nearby(latitude: float, longitude: float):
    point = Point(type="Point", coordinates=[longitude, latitude])
    nearby_audios = audios.find(
        {"location": {"$nearSphere": {"$geometry": point.dict(), "$maxDistance": 20000}}}
    )
    audio_list = []
    for audio in nearby_audios:
        audio['_id'] = str(audio['_id'])  # Convert ObjectId to string
        audio_list.append(audio)
    return {"audios_nearby": audio_list}


# Create an endpoint to handle image deletions
@app.delete("/audios/{entry_id}")
async def delete_image(entry_id: str):
    # Find the document in the collection with the given ID
    audio = audios.find_one({"entry_id": int(entry_id)})

    if audio is None:
        return {"message": "Sound not found"}
    
    # Delete the image file from disk
    try:
        os.remove(audio["filename"])
    except FileNotFoundError:
        print("File not found")
    except PermissionError:
        print("Permission denied")

    # Delete the document from the collection
    audios.delete_one({"entry_id": int(entry_id)})

    # Return a success message as a JSON response
    return {"message": "Sound deleted"}


# Create an endpoint to get locations within a 20km radius
@app.get("/audios/nearby_stacking/")
async def get_audios_nearby(latitude: float, longitude: float):
    point = Point(type="Point", coordinates=[longitude, latitude])
    nearby_audios = audios.find(
        {"location": {"$nearSphere": {"$geometry": point.dict(), "$maxDistance": 100}}}
    )
    audio_list = []
    for audio in nearby_audios:
        audio['_id'] = str(audio['_id'])  # Convert ObjectId to string
        audio_list.append(audio)
    return {"audios_nearby": audio_list}


@app.post("/audios/{entry_id}/like_dislike")
async def like_dislike_audio(entry_id: str, like: bool = Query(..., description="True for like, False for dislike")):
    # Find the document in the collection with the given ID
    audio = audios.find_one({"entry_id": int(entry_id)})

    if audio is None:
        raise HTTPException(status_code=404, detail="audio not found")
    
    # Update the number of likes or dislikes for this audio
    if like:
        audios.update_one({"entry_id": int(entry_id)}, {"$inc": {"num_likes": 1}})
    else:
        audios.update_one({"entry_id": int(entry_id)}, {"$inc": {"num_dislikes": 1}})

    # Return a success message as a JSON response
    return {"message": "Action successful"}
