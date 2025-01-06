// Event handler for to update autocomplete suggestions for the tag filter input
const input = document.getElementById("tag-input");
input.addEventListener("input", debounce(function(e) {
    const currentValue = this.value;

    //starting autocomplete at 1 character
    if (!currentValue || currentValue.length < 1) {
        closeDropDownList();
        return;
    }

    // Fetch autocomplete suggestions
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

            data.forEach(tagString => {
                const itemElement = document.createElement("div");
                itemElement.innerText = tagString;
                itemElement.tabIndex = 0;
                itemElement.addEventListener("click", function() {
                    input.value = tagString;
                    addTagToFilter()
                    closeDropDownList();
                });
                itemElement.addEventListener("keydown", function(e) {
                    if(e.key === "Enter"){
                        input.value = tagString;
                        addTagToFilter()
                        closeDropDownList();
                    }
                });
                autocompleteItemsElement.appendChild(itemElement);
            });
        })
        .catch(error => {
            console.error('Error fetching data:', error);
        });
}, 300));

