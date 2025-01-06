// Close the suggestions when user click elsewhere of the screen
document.body.addEventListener("mousedown", function (e) {
    const autocompleteItemsElement = document.getElementById("autocomplete-items");
    if (!autocompleteItemsElement.contains(e.target)) {
        closeDropDownList();
    }
})
// Close the suggestions when user tab outside of suggestions
document.body.addEventListener("focusin", function(e) {
    const autocompleteItemsElement = document.getElementById("autocomplete-items");
    if (!autocompleteItemsElement.contains(e.target)) {
        closeDropDownList();
    }
});

var currentItems;

function debounce(func, delay) {
    let timerId;

    return function() {
        const context = this;
        const args = arguments;

        clearTimeout(timerId);

        timerId = setTimeout(() => {
            func.apply(context, args);
        }, delay);
    };
}

const input = document.getElementById("location");
input.addEventListener("keydown", (function(e) {
    if (e.key === "Delete") {
        closeDropDownList()
    }
}));
input.addEventListener("input", debounce(function(e) {

    const currentValue = this.value.replaceAll('/', "thisisaslash123");

    if (!currentValue || currentValue.length < 3) {
        return false;
    }

    const encodedValue = encodeURIComponent(currentValue);
    const url = window.location.href.split("profile")[0] + `address/${encodedValue}`;

    fetch(url)
        .then(res => {
            if (!res.ok) {
                return null;
            } else {
                return res.json();
            }
        })
        .then(data => {
            if (!data) {
                return;
            }

            currentItems = data;

            const autocompleteItemsElement = document.getElementById("autocomplete-items");

            // Ensure autocompleteItemsElement is present
            if (!autocompleteItemsElement) {
                console.error("Element with ID 'autocomplete-items' not found.");
                return;
            }

            closeDropDownList();
            data.forEach((location, index) => {
                const itemElement = document.createElement("div");
                itemElement.setAttribute("tabindex", "0");
                itemElement.innerHTML = location.formatted;
                itemElement.addEventListener("keydown", function (event) {
                    if(event.key === "Enter") {
                        itemElement.click();
                    }
                })
                autocompleteItemsElement.appendChild(itemElement);
                itemElement.addEventListener("click", function(e) {
                    closeDropDownList();
                    const countryElement = document.getElementById("country");
                    const cityElement = document.getElementById("city");
                    const suburbElement = document.getElementById("suburb");
                    const streetElement = document.getElementById("street");
                    const postcodeElement = document.getElementById("postcode");

                    const selectedLocation = currentItems[index]; // Use different variable to avoid shadowing
                    input.value = selectedLocation.formatted;
                    countryElement.value = selectedLocation.country ? selectedLocation.country : "";
                    cityElement.value = selectedLocation.city ? selectedLocation.city : "";
                    suburbElement.value = selectedLocation.suburb ? selectedLocation.suburb : "";
                    streetElement.value = selectedLocation.street ? selectedLocation.street : "";
                    postcodeElement.value = selectedLocation.postcode ? selectedLocation.postcode : "";

                    triggerEvent(countryElement, "input")
                    triggerEvent(cityElement, "input")
                });
            });

            if (data.length === 0) {
                // Get the message for the no matches error from the hidden element
                var noMatchesText = document.getElementById('no-matches-hidden-text').innerText;
                const noMatchElement = document.createElement("div");
                noMatchElement.innerHTML = noMatchesText;
                autocompleteItemsElement.appendChild(noMatchElement);
            }

            // Get the message to start the credit attribution with from the hidden element
            var attributionStarter = document.getElementById('attribution-starter-hidden-text').innerText;
            const creditAttribute = document.createElement("div");
            creditAttribute.innerHTML = attributionStarter + ' <a href="https://www.geoapify.com/" target="_blank">Geoapify</a>';
            autocompleteItemsElement.appendChild(creditAttribute);

        })
        .catch(error => {
            console.error('Error fetching data:', error);
        });
}, 700));

function closeDropDownList() {
    const autocompleteItemsElement = document.getElementById("autocomplete-items");
    if (autocompleteItemsElement) {
        while (autocompleteItemsElement.firstChild) {
            autocompleteItemsElement.removeChild(autocompleteItemsElement.firstChild);
        }
    }
}

/**
 * Triggers the specified event on the given element
 * @param {HTMLElement} element - The element on which to trigger the event
 * @param {string} eventName - The name of the event to trigger
 */
function triggerEvent(element, eventName) {
    const event = new Event(eventName, {
        bubbles: true,
        cancelable: true,
    });
    element.dispatchEvent(event);
}

