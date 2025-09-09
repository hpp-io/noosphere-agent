import React from 'react';
import { FormGroup, Label, Input, FormText, Row, Col, Card, CardHeader, CardBody } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { ApplicationProperties } from 'app/shared/model/application-configuration.model';

interface ServerConfigPanelProps {
  config: ApplicationProperties;
  onChange: (config: ApplicationProperties) => void;
  errors?: any;
}

const ServerConfigPanel: React.FC<ServerConfigPanelProps> = ({ config, onChange, errors = {} }) => {
  const handleServerChange = (field: string, value: any) => {
    onChange({
      ...config,
      server: {
        ...config.server,
        [field]: value,
      },
    });
  };

  const handleRateLimitChange = (field: string, value: any) => {
    onChange({
      ...config,
      server: {
        ...config.server,
        rateLimit: {
          ...config.server?.rateLimit,
          [field]: value,
        },
      },
    });
  };

  return (
    <div className="server-config-panel">
      <Card className="mb-4">
        <CardHeader>
          <h5 className="mb-0">
            <Translate contentKey="applicationConfiguration.server.title">Server Configuration</Translate>
          </h5>
        </CardHeader>
        <CardBody>
          <FormGroup>
            <Label for="serverPort">
              <Translate contentKey="applicationConfiguration.server.fields.port.label">Server Port</Translate>
            </Label>
            <Input
              type="number"
              name="serverPort"
              id="serverPort"
              value={config.server?.port || 8080}
              onChange={e => handleServerChange('port', parseInt(e.target.value, 10))}
              min="1"
              max="65535"
              invalid={!!errors.server?.port}
            />
            <FormText color="muted">
              <Translate contentKey="applicationConfiguration.server.fields.port.help">Port number for the server</Translate>
            </FormText>
            {errors.server?.port && <FormText color="danger">{errors.server.port}</FormText>}
          </FormGroup>
        </CardBody>
      </Card>

      {/* Rate Limiting Configuration */}
      <Card className="mb-4">
        <CardHeader>
          <h6 className="mb-0">
            <Translate contentKey="applicationConfiguration.server.rateLimit.title">Rate Limiting</Translate>
          </h6>
        </CardHeader>
        <CardBody>
          <Row>
            <Col md={6}>
              <FormGroup>
                <Label for="numRequests">
                  <Translate contentKey="applicationConfiguration.server.rateLimit.fields.numRequests.label">Number of Requests</Translate>
                </Label>
                <Input
                  type="number"
                  name="numRequests"
                  id="numRequests"
                  value={config.server?.rateLimit?.numRequests || 100}
                  onChange={e => handleRateLimitChange('numRequests', parseInt(e.target.value, 10))}
                  min="1"
                  invalid={!!errors.server?.rateLimit?.numRequests}
                />
                <FormText color="muted">
                  <Translate contentKey="applicationConfiguration.server.rateLimit.fields.numRequests.help">
                    Maximum number of requests allowed
                  </Translate>
                </FormText>
                {errors.server?.rateLimit?.numRequests && <FormText color="danger">{errors.server.rateLimit.numRequests}</FormText>}
              </FormGroup>
            </Col>
            <Col md={6}>
              <FormGroup>
                <Label for="ratePeriod">
                  <Translate contentKey="applicationConfiguration.server.rateLimit.fields.period.label">Time Period</Translate>
                </Label>
                <Input
                  type="number"
                  name="ratePeriod"
                  id="ratePeriod"
                  value={config.server?.rateLimit?.period || 60}
                  onChange={e => handleRateLimitChange('period', parseInt(e.target.value, 10))}
                  min="1"
                  invalid={!!errors.server?.rateLimit?.period}
                />
                <FormText color="muted">
                  <Translate contentKey="applicationConfiguration.server.rateLimit.fields.period.help">
                    Time period for rate limiting (seconds)
                  </Translate>
                </FormText>
                {errors.server?.rateLimit?.period && <FormText color="danger">{errors.server.rateLimit.period}</FormText>}
              </FormGroup>
            </Col>
          </Row>
        </CardBody>
      </Card>
    </div>
  );
};

export default ServerConfigPanel;
