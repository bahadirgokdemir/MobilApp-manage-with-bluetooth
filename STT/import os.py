import os
from flask import Flask, request, jsonify
import whisper
app = Flask(__name__)
print("server başladı")
@app.route('/upload', methods=['POST'])
def upload_file():
    print("def'in içi")
    try:

        model = whisper.load_model("base.en")

        print("model yüklendi")
        if 'file' not in request.files:
            print("no file part")
            return "No file part"
        file = request.files['file']
        if file.filename == '':
            return "No selected file"
        if file:
            print("dosyayı gördü")
            print(file)
            audio = whisper.load_audio(file)
            print("audioyu aldı")
            audio = whisper.pad_or_trim(audio)
            print("pad or trim")

            mel = whisper.log_mel_spectrogram(audio).to(model.device)
            print("mel spektogramı")

            options = whisper.DecodingOptions()
            print("options")
            result = whisper.decode(model, mel, options)
            print("result")
            try:
                print("try'ın içine girdi")
                text = result.text
                return jsonify({"text": text})
            
            except:
                print("could not understand the audio")
                return jsonify({"error": "Could not understand the audio"})
            
            
    except Exception as e:
        print("büyük exception")
        # Hata durumunda günlük dosyasına bilgileri kaydet
        with open("error.log", "a") as f:
            f.write(f"An error occurred: {str(e)}\n")
        return jsonify({"error": "%s\n\nAn error occurred, please check the server logs"}), 500

@app.route('/')
def home():
    return "Hello, Flask!"

if __name__ == '__main__':
    app.run(debug=True, host='192.168.0.11', port=5000, ssl_context=('server.crt', 'server.key'))
