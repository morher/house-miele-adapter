# Miele adapter
Monitors Miele house appliances.

The adapter will report the current `Status`, `Program phase`, `Remaining time` and `Estimated complete time` for the mapped devices. Data will be polled every 30 seconds.

## Configuration
This adapter uses the [Miele API](https://www.miele.com/developer/index.html). Register your instance to get a `clientId` and `clientSecret`. Then provide the `email` and `password` used in the Miele app. In addition, the `location` for the user must be provided.

The Miele devices must then be mapped to house-devices. When the adapter starts available Miele devices will be logged to the console.

### Example
```yaml
miele:
   clientId: 'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx'
   clientSecret: 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'
   email: 'mail@example.com'
   password: 'passw0rd'
   location: 'no-NO'

   devices:
      000000000001:
         device:
            room: Kitchen
            name: Dishwasher
```
