import { getBalances, getProducts } from "api";
import { useEffect, useState } from "react";
import { Alert, Container, ListGroup } from "react-bootstrap";
import { Balances, UserCredentials } from "./types";



const Account = ({ userCredentials }: { userCredentials: Partial<UserCredentials> }) => {

  const [balances, setBalances] = useState<Balances>({});
  const [products, setProducts] = useState<string[]>([]);

  useEffect(() => {
    getBalances(userCredentials).then((balances) => setBalances(balances))
  }, [])

  useEffect(() => {
    getProducts(userCredentials).then((products) => setProducts(products))
  }, [])

  return (
    <Container>
      <Alert className="mt-1" variant="success" data-test-id="welcome-user">Welcome, {userCredentials.username}</Alert>

      <ListGroup className="mt-1" data-test-id="balances">
        <ListGroup.Item variant="primary">Available Balances</ListGroup.Item>
        {Object.keys(balances).map(asset => (
          <ListGroup.Item>{asset}: {balances[asset]}</ListGroup.Item>
        ))}
      </ListGroup>

      <ListGroup className="mt-1" data-test-id="products">
        <ListGroup.Item variant="primary">Tradeable Products</ListGroup.Item>
        {products.map(product => (
          <ListGroup.Item action data-test-id={`product-${product}`} onClick={() => redirectToTrading(product)}>{product}</ListGroup.Item>
        ))}
      </ListGroup>
    </Container>
  )
};


const redirectToTrading: (product: string) => void = (product) => {
  document.location.href = `/trade/${product}`
}


export default Account;
