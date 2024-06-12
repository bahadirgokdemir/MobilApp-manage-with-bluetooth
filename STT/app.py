import os
from flask import Flask, request, jsonify
import whisper

app = Flask(__name__)
units = {
    "zero": 0, "one": 1, "two": 2, "three": 3, "four": 4, 
    "five": 5, "six": 6, "seven": 7, "eight": 8, "nine": 9
}

teens = {
    "ten": 10, "eleven": 11, "twelve": 12, "thirteen": 13, "fourteen": 14, 
    "fifteen": 15, "sixteen": 16, "seventeen": 17, "eighteen": 18, "nineteen": 19
}

tens = {
    "twenty": 20, "thirty": 30, "forty": 40, "fifty": 50, 
    "sixty": 60, "seventy": 70, "eighty": 80, "ninety": 90
}

def trim_spaces(text):
    return text.strip()

def remove_trailing_dot(text):
    if text and text.endswith('.'):
        return text[:-1]
    return text

def text_to_number(text):
    words = text.split()
    number = 0
    
    for word in words:
        if word in units:
            number += units[word]
        elif word in teens:
            number += teens[word]
        elif word in tens:
            number += tens[word]
    
    if len(words) == 2:
        if words[0] in tens and words[1] in units:
            number = tens[words[0]] + units[words[1]]
    
    return number

def correct_recognition_errors(recognized_text):
    corrections = {
        "ken": "ten",
        "too": "two",
        "to": "two",
        "tree": "three",
        "for": "four",
        "fife": "five",
        "sex": "six",
        "ate": "eight",
        "won": "one",
        "fiv": "five",
        "tweny": "twenty",
        "tirty": "thirty",
        "forty one": "forty-one",
        "fifty one": "fifty-one",
        "sixty one": "sixty-one",
        "seventy one": "seventy-one",
        "eighty one": "eighty-one",
        "ninety one": "ninety-one",
        "forty two": "forty-two",
        "fifty two": "fifty-two",
        "sixty two": "sixty-two",
        "seventy two": "seventy-two",
        "eighty two": "eighty-two",
        "ninety two": "ninety-two",
        "forty three": "forty-three",
        "fifty three": "fifty-three",
        "sixty three": "sixty-three",
        "seventy three": "seventy-three",
        "eighty three": "eighty-three",
        "ninety three": "ninety-three",
        "forty four": "forty-four",
        "fifty four": "fifty-four",
        "sixty four": "sixty-four",
        "seventy four": "seventy-four",
        "eighty four": "eighty-four",
        "ninety four": "ninety-four",
        "forty five": "forty-five",
        "fifty five": "fifty-five",
        "sixty five": "sixty-five",
        "seventy five": "seventy-five",
        "eighty five": "eighty-five",
        "ninety five": "ninety-five",
        "forty six": "forty-six",
        "fifty six": "fifty-six",
        "sixty six": "sixty-six",
        "seventy six": "seventy-six",
        "eighty six": "eighty-six",
        "ninety six": "ninety-six",
        "forty seven": "forty-seven",
        "fifty seven": "fifty-seven",
        "sixty seven": "sixty-seven",
        "seventy seven": "seventy-seven",
        "eighty seven": "eighty-seven",
        "ninety seven": "ninety-seven",
        "forty eight": "forty-eight",
        "fifty eight": "fifty-eight",
        "sixty eight": "sixty-eight",
        "seventy eight": "seventy-eight",
        "eighty eight": "eighty-eight",
        "ninety eight": "ninety-eight",
        "forty nine": "forty-nine",
        "fifty nine": "fifty-nine",
        "sixty nine": "sixty-nine",
        "seventy nine": "seventy-nine",
        "eighty nine": "eighty-nine",
        "ninety nine": "ninety-nine"
    }
    
    for error, correction in corrections.items():
        recognized_text = recognized_text.replace(error, correction)
    
    return recognized_text

UPLOAD_FOLDER = 'uploads'  # Göreceli bir yol kullanmak daha güvenli olabilir
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

print("server başladı")

@app.route('/upload', methods=['POST'])
def upload_file():
    print("def'in içi")
    try:
        model = whisper.load_model("small.en")
        print("model yüklendi")

        if 'file' not in request.files:
            return "No file part"
        file = request.files['file']
        if file.filename == '':
            return "No selected file"
        if file:
            # Dosyayı sunucuda belirlenen dizine kaydet
            file_path = os.path.join(app.config['UPLOAD_FOLDER'], file.filename)
            file.save(file_path)
            print("Dosya kaydedildi:", file_path)

            # Dosya yolunu kullanarak Whisper modelini çalıştır
            result = model.transcribe(file_path)
            trimmed_text = trim_spaces(result['text'])
            dot_removed_text = remove_trailing_dot(trimmed_text)
            corrected_text = correct_recognition_errors(dot_removed_text.lower())
            number = text_to_number(corrected_text)
            print("result budur:", corrected_text)

            try:
                print("try'ın içine girdi")
                text = corrected_text
                print(text)
                return text
            except:
                print("could not understand the audio")
                return jsonify({"error": "Could not understand the audio"})

    except Exception as e:
        print("büyük exception:", str(e))
        # Hata durumunda günlük dosyasına bilgileri kaydet
        with open("error.log", "a") as f:
            f.write(f"An error occurred: {str(e)}\n")
        return jsonify({"error": f"{str(e)}\n\nAn error occurred, please check the server logs"}), 500

@app.route('/')
def home():
    return "Hello, Flask!"

if __name__ == '__main__':
    app.run(debug=True, host='192.168.0.11', port=5000, ssl_context=('server.crt', 'server.key'))
