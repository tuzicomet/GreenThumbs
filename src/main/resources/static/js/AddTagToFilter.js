
const tagInput = document.getElementById("tag-input")
const BASE_URL = window.location.href.split("/browseGardens")[0]

// This event listener is used to prevent tags that do not exist in the database from being added as a filter
tagInput.addEventListener("keydown", function(event) {
    if (event.key !== "Enter") {
        return;
    }
    event.preventDefault();
    const currentValue = this.value;
    fetch(`${BASE_URL}/tags/autocomplete?query=${encodeURIComponent(currentValue)}`)
        .then(res => {
            if (!res.ok){
                return null;
            } else{
                return res.json();
            }
        })
        .then(data => {
            if(data === null){return null;}
            closeDropDownList();
            const autocompleteItemsElement = document.getElementById("autocomplete-items");

            // Ensure autocompleteItemsElement is present
            if (!autocompleteItemsElement) {
                console.error("Element with ID 'autocomplete-items' not found.");
                return;
            }
            let matched = false;
            // checks if the exact input is in the autocomplete suggestions
            // if it exists, it will always be in the autocomplete suggestions
            data.forEach(tagString => {
                if(tagString === currentValue){
                    matched = true;
                    setStyleValid(tagInput);
                    addTagToFilter();
                }
            });
            if(!matched){showErrorPopUp(tagInput, errorNoTagMatching + this.value);
                setStyleInvalid(tagInput);
            }


        })
        .catch(error => {
            console.error('Error fetching data:', error);
        });
});

/**
 * Adds the tag from the input box to the list of tags t filter by
 * this includes adding it to the visual list of tags as well as adding it t the ';' delimited string of tags.
 */
function addTagToFilter(){
    const chosenTagsField = document.getElementById("chosen-tags");
    const newTag = document.getElementById("tag-input");
    if(chosenTagsField.value.split(";").includes(newTag.value)){showErrorPopUp(newTag, errorTagAlreadySelected); setStyleInvalid(tagInput); return}
    chosenTagsField.value= chosenTagsField.value === ""? chosenTagsField.value + newTag.value : chosenTagsField.value + ";" + newTag.value
    const p = document.createElement('li')
    p.innerText = newTag.value
    const parent = document.getElementById("form")
    parent.insertBefore(p, chosenTagsField)
    newTag.value = ""
    setStyleDefault(tagInput);
}

tagInput.addEventListener("input", function() {
    setStyleDefault(tagInput);
})