export type Asset = {
    assetId: number
    symbol: string
    scale: number
}

export type GetAssetsResponse = {
    assets: Asset[]
}