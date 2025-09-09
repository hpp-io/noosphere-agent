import React, { useState } from 'react';
import { FormGroup, Label, Input, FormText, Row, Col, Card, CardHeader, CardBody, Button, Table } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { ApplicationProperties, NoosphereContainer } from 'app/shared/model/application-configuration.model';

interface ContainerConfigPanelProps {
  config: ApplicationProperties;
  onChange: (config: ApplicationProperties) => void;
  errors?: any;
}

const ContainerConfigPanel: React.FC<ContainerConfigPanelProps> = ({ config, onChange, errors = {} }) => {
  const [selectedContainerIndex, setSelectedContainerIndex] = useState<number>(0);

  const handleContainerChange = (index: number, field: string, value: any) => {
    const updatedContainers = [...(config.containers || [])];
    updatedContainers[index] = {
      ...updatedContainers[index],
      [field]: value,
    };

    onChange({
      ...config,
      containers: updatedContainers,
    });
  };

  const addContainer = () => {
    const newContainer: NoosphereContainer = {
      id: '',
      image: '',
      url: '',
      bearer: '',
      port: 8080,
      external: false,
      gpu: false,
      acceptedPayments: {},
      allowedIps: [],
      allowedAddresses: [],
      allowedDelegateAddresses: [],
      description: '',
      command: '',
      env: {},
      generatesProofs: false,
      volumes: [],
    };

    onChange({
      ...config,
      containers: [...(config.containers || []), newContainer],
    });
    setSelectedContainerIndex((config.containers || []).length);
  };

  const removeContainer = (index: number) => {
    const updatedContainers = [...(config.containers || [])];
    updatedContainers.splice(index, 1);
    onChange({
      ...config,
      containers: updatedContainers,
    });
    if (selectedContainerIndex >= updatedContainers.length) {
      setSelectedContainerIndex(Math.max(0, updatedContainers.length - 1));
    }
  };

  const containers = config.containers || [];
  const selectedContainer = containers[selectedContainerIndex];

  return (
    <div className="container-config-panel">
      <Card className="mb-4">
        <CardHeader className="d-flex justify-content-between align-items-center">
          <h5 className="mb-0">
            <Translate contentKey="applicationConfiguration.containers.title">Container Configuration</Translate>
          </h5>
          <Button color="primary" size="sm" onClick={addContainer}>
            <Translate contentKey="applicationConfiguration.containers.actions.add">Add Container</Translate>
          </Button>
        </CardHeader>
        <CardBody>
          {containers.length === 0 ? (
            <p className="text-muted">No containers configured. Add a container to get started.</p>
          ) : (
            <Row>
              <Col md={4}>
                <div className="container-list">
                  <h6>Containers</h6>
                  <div className="list-group">
                    {containers.map((container, index) => (
                      <button
                        key={index}
                        type="button"
                        className={`list-group-item list-group-item-action ${selectedContainerIndex === index ? 'active' : ''}`}
                        onClick={() => setSelectedContainerIndex(index)}
                      >
                        <div className="d-flex w-100 justify-content-between">
                          <h6 className="mb-1">{container.id || `Container ${index + 1}`}</h6>
                          <Button
                            color="danger"
                            size="sm"
                            onClick={e => {
                              e.stopPropagation();
                              removeContainer(index);
                            }}
                          >
                            ×
                          </Button>
                        </div>
                        <p className="mb-1">{container.image}</p>
                      </button>
                    ))}
                  </div>
                </div>
              </Col>
              <Col md={8}>
                {selectedContainer && (
                  <div className="container-details">
                    <h6>Container Details</h6>
                    <Row>
                      <Col md={6}>
                        <FormGroup>
                          <Label for="containerId">
                            <Translate contentKey="applicationConfiguration.containers.fields.id.label">Container ID</Translate>
                          </Label>
                          <Input
                            type="text"
                            name="containerId"
                            id="containerId"
                            value={selectedContainer.id || ''}
                            onChange={e => handleContainerChange(selectedContainerIndex, 'id', e.target.value)}
                            invalid={!!errors.containers?.[selectedContainerIndex]?.id}
                          />
                          <FormText color="muted">
                            <Translate contentKey="applicationConfiguration.containers.fields.id.help">
                              Unique identifier for the container
                            </Translate>
                          </FormText>
                          {errors.containers?.[selectedContainerIndex]?.id && (
                            <FormText color="danger">{errors.containers[selectedContainerIndex].id}</FormText>
                          )}
                        </FormGroup>
                      </Col>
                      <Col md={6}>
                        <FormGroup>
                          <Label for="containerImage">
                            <Translate contentKey="applicationConfiguration.containers.fields.image.label">Docker Image</Translate>
                          </Label>
                          <Input
                            type="text"
                            name="containerImage"
                            id="containerImage"
                            value={selectedContainer.image || ''}
                            onChange={e => handleContainerChange(selectedContainerIndex, 'image', e.target.value)}
                            invalid={!!errors.containers?.[selectedContainerIndex]?.image}
                          />
                          <FormText color="muted">
                            <Translate contentKey="applicationConfiguration.containers.fields.image.help">
                              Docker image name and tag
                            </Translate>
                          </FormText>
                          {errors.containers?.[selectedContainerIndex]?.image && (
                            <FormText color="danger">{errors.containers[selectedContainerIndex].image}</FormText>
                          )}
                        </FormGroup>
                      </Col>
                    </Row>

                    <Row>
                      <Col md={6}>
                        <FormGroup>
                          <Label for="containerUrl">
                            <Translate contentKey="applicationConfiguration.containers.fields.url.label">Container URL</Translate>
                          </Label>
                          <Input
                            type="text"
                            name="containerUrl"
                            id="containerUrl"
                            value={selectedContainer.url || ''}
                            onChange={e => handleContainerChange(selectedContainerIndex, 'url', e.target.value)}
                            invalid={!!errors.containers?.[selectedContainerIndex]?.url}
                          />
                          <FormText color="muted">
                            <Translate contentKey="applicationConfiguration.containers.fields.url.help">
                              URL to access the container service
                            </Translate>
                          </FormText>
                          {errors.containers?.[selectedContainerIndex]?.url && (
                            <FormText color="danger">{errors.containers[selectedContainerIndex].url}</FormText>
                          )}
                        </FormGroup>
                      </Col>
                      <Col md={6}>
                        <FormGroup>
                          <Label for="containerPort">
                            <Translate contentKey="applicationConfiguration.containers.fields.port.label">Port</Translate>
                          </Label>
                          <Input
                            type="number"
                            name="containerPort"
                            id="containerPort"
                            value={selectedContainer.port || 8080}
                            onChange={e => handleContainerChange(selectedContainerIndex, 'port', parseInt(e.target.value, 10))}
                            min="1"
                            max="65535"
                            invalid={!!errors.containers?.[selectedContainerIndex]?.port}
                          />
                          <FormText color="muted">
                            <Translate contentKey="applicationConfiguration.containers.fields.port.help">
                              Port number for the container service
                            </Translate>
                          </FormText>
                          {errors.containers?.[selectedContainerIndex]?.port && (
                            <FormText color="danger">{errors.containers[selectedContainerIndex].port}</FormText>
                          )}
                        </FormGroup>
                      </Col>
                    </Row>

                    <FormGroup>
                      <Label for="containerBearer">
                        <Translate contentKey="applicationConfiguration.containers.fields.bearer.label">Bearer Token</Translate>
                      </Label>
                      <Input
                        type="password"
                        name="containerBearer"
                        id="containerBearer"
                        value={selectedContainer.bearer || ''}
                        onChange={e => handleContainerChange(selectedContainerIndex, 'bearer', e.target.value)}
                        invalid={!!errors.containers?.[selectedContainerIndex]?.bearer}
                      />
                      <FormText color="muted">
                        <Translate contentKey="applicationConfiguration.containers.fields.bearer.help">
                          Authentication token for the container
                        </Translate>
                      </FormText>
                      {errors.containers?.[selectedContainerIndex]?.bearer && (
                        <FormText color="danger">{errors.containers[selectedContainerIndex].bearer}</FormText>
                      )}
                    </FormGroup>

                    <Row>
                      <Col md={6}>
                        <FormGroup check>
                          <Label check>
                            <Input
                              type="checkbox"
                              checked={selectedContainer.external || false}
                              onChange={e => handleContainerChange(selectedContainerIndex, 'external', e.target.checked)}
                            />{' '}
                            <Translate contentKey="applicationConfiguration.containers.fields.external.label">External Container</Translate>
                          </Label>
                          <FormText color="muted">
                            <Translate contentKey="applicationConfiguration.containers.fields.external.help">
                              Whether this is an external container
                            </Translate>
                          </FormText>
                        </FormGroup>
                      </Col>
                      <Col md={6}>
                        <FormGroup check>
                          <Label check>
                            <Input
                              type="checkbox"
                              checked={selectedContainer.gpu || false}
                              onChange={e => handleContainerChange(selectedContainerIndex, 'gpu', e.target.checked)}
                            />{' '}
                            <Translate contentKey="applicationConfiguration.containers.fields.gpu.label">GPU Required</Translate>
                          </Label>
                          <FormText color="muted">
                            <Translate contentKey="applicationConfiguration.containers.fields.gpu.help">
                              Whether this container requires GPU access
                            </Translate>
                          </FormText>
                        </FormGroup>
                      </Col>
                    </Row>

                    <FormGroup>
                      <Label for="containerDescription">
                        <Translate contentKey="applicationConfiguration.containers.fields.description.label">Description</Translate>
                      </Label>
                      <Input
                        type="textarea"
                        name="containerDescription"
                        id="containerDescription"
                        value={selectedContainer.description || ''}
                        onChange={e => handleContainerChange(selectedContainerIndex, 'description', e.target.value)}
                        rows={3}
                      />
                      <FormText color="muted">
                        <Translate contentKey="applicationConfiguration.containers.fields.description.help">
                          Human-readable description of the container
                        </Translate>
                      </FormText>
                    </FormGroup>

                    <FormGroup>
                      <Label for="containerCommand">
                        <Translate contentKey="applicationConfiguration.containers.fields.command.label">Command</Translate>
                      </Label>
                      <Input
                        type="text"
                        name="containerCommand"
                        id="containerCommand"
                        value={selectedContainer.command || ''}
                        onChange={e => handleContainerChange(selectedContainerIndex, 'command', e.target.value)}
                      />
                      <FormText color="muted">
                        <Translate contentKey="applicationConfiguration.containers.fields.command.help">
                          Command to run in the container
                        </Translate>
                      </FormText>
                    </FormGroup>

                    <FormGroup check>
                      <Label check>
                        <Input
                          type="checkbox"
                          checked={selectedContainer.generatesProofs || false}
                          onChange={e => handleContainerChange(selectedContainerIndex, 'generatesProofs', e.target.checked)}
                        />{' '}
                        <Translate contentKey="applicationConfiguration.containers.fields.generatesProofs.label">
                          Generates Proofs
                        </Translate>
                      </Label>
                      <FormText color="muted">
                        <Translate contentKey="applicationConfiguration.containers.fields.generatesProofs.help">
                          Whether this container generates cryptographic proofs
                        </Translate>
                      </FormText>
                    </FormGroup>
                  </div>
                )}
              </Col>
            </Row>
          )}
        </CardBody>
      </Card>
    </div>
  );
};

export default ContainerConfigPanel;
