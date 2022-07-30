import { Asset } from "Assets/assets"
import { Assets } from "Assets/Assets"
import { Products } from "Products/Products"
import { useState } from "react"
import { BrowserRouter, Route } from "react-router-dom"
import { UserPage, Users } from "Users/Users"

const App = () => {

    const [assets, setAssets] = useState<Asset[]>([])

    return (
        <BrowserRouter>
            <Route exact path="/user/:userId" component={UserPage} />
            <Route exact path="/" component={Users}/>
            <Route exact path="/" render={() => <Assets assets={assets} setAssets={setAssets} />} />
            <Route exact path="/" component={() => <Products assets={assets} />}/>
        </BrowserRouter>
    )
}

export default App
