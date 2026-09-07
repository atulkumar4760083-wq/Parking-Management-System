import requests
import json
from typing import Dict, Optional

class JokeGenerator:
    """
    A random joke generator that uses external APIs to fetch jokes.
    Supports multiple joke sources:
    - Official Joke API
    - JokeAPI
    """
    
    def __init__(self):
        self.official_joke_api = "https://official-joke-api.appspot.com/random_joke"
        self.joke_api = "https://v2.jokeapi.dev/joke/Any"
        self.timeout = 5  # seconds
    
    def get_random_joke_official(self) -> Optional[Dict]:
        """
        Fetch a random joke from Official Joke API.
        
        Returns:
            Dict with 'setup' and 'punchline' keys, or None if request fails
        """
        try:
            response = requests.get(self.official_joke_api, timeout=self.timeout)
            response.raise_for_status()
            joke_data = response.json()
            
            return {
                'type': 'two-part',
                'setup': joke_data.get('setup', ''),
                'punchline': joke_data.get('punchline', ''),
                'source': 'Official Joke API'
            }
        except requests.exceptions.RequestException as e:
            print(f"Error fetching from Official Joke API: {e}")
            return None
    
    def get_random_joke_jokeapi(self) -> Optional[Dict]:
        """
        Fetch a random joke from JokeAPI.
        
        Returns:
            Dict with joke content, or None if request fails
        """
        try:
            response = requests.get(self.joke_api, timeout=self.timeout)
            response.raise_for_status()
            joke_data = response.json()
            
            if joke_data.get('error'):
                print(f"API Error: {joke_data.get('message')}")
                return None
            
            joke_type = joke_data.get('type', 'single')
            
            if joke_type == 'single':
                return {
                    'type': 'single',
                    'joke': joke_data.get('joke', ''),
                    'source': 'JokeAPI'
                }
            else:  # two-part
                return {
                    'type': 'two-part',
                    'setup': joke_data.get('setup', ''),
                    'delivery': joke_data.get('delivery', ''),
                    'source': 'JokeAPI'
                }
        except requests.exceptions.RequestException as e:
            print(f"Error fetching from JokeAPI: {e}")
            return None
    
    def print_joke(self, joke: Dict) -> None:
        """
        Pretty print a joke to console.
        
        Args:
            joke: Dictionary containing joke data
        """
        if not joke:
            print("Could not fetch a joke. Please try again.")
            return
        
        print(f"\n{'='*60}")
        print(f"Source: {joke.get('source', 'Unknown')}")
        print(f"{'='*60}")
        
        if joke.get('type') == 'single':
            print(f"😂 {joke.get('joke')}")
        else:
            print(f"🤔 {joke.get('setup', joke.get('setup', ''))}")
            print(f"😄 {joke.get('punchline', joke.get('delivery', ''))}")
        
        print(f"{'='*60}\n")


def main():
    """Main function to demonstrate the joke generator."""
    generator = JokeGenerator()
    
    print("🎭 Welcome to the Random Joke Generator! 🎭")
    
    # Fetch jokes from both sources
    print("\n1. Fetching from Official Joke API...")
    joke1 = generator.get_random_joke_official()
    if joke1:
        generator.print_joke(joke1)
    
    print("2. Fetching from JokeAPI...")
    joke2 = generator.get_random_joke_jokeapi()
    if joke2:
        generator.print_joke(joke2)
    
    # Interactive mode
    print("\n" + "="*60)
    print("Interactive Mode - Get more jokes!")
    print("="*60)
    
    while True:
        choice = input("\nChoose an option:\n1. Get joke from Official Joke API\n2. Get joke from JokeAPI\n3. Exit\n\nEnter choice (1-3): ").strip()
        
        if choice == '1':
            joke = generator.get_random_joke_official()
            generator.print_joke(joke)
        elif choice == '2':
            joke = generator.get_random_joke_jokeapi()
            generator.print_joke(joke)
        elif choice == '3':
            print("\nThanks for using the Joke Generator! 👋")
            break
        else:
            print("Invalid choice. Please try again.")


if __name__ == "__main__":
    main()
